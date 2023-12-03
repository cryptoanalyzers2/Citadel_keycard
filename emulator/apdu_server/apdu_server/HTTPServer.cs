
using System;
using System.Net;
using System.IO;
using System.Text;
using PCSC.Iso7816;
using PCSC;
using System.Formats.Asn1;
using GS.SCard;

public class HttpServer
{
    public int Port = 38099;

    private HttpListener _listener;
    IContextFactory contextFactory = ContextFactory.Instance;
    //string emulator_reader="JAVACOS Virtual Contact Reader 0";
    string emulator_reader = "Identive SCR33xx v2.0 USB SC Reader 0";
    CommandApdu apdu_select;


    public static void WriteRedLine(String msg)
    {


        //Console.BackgroundColor = ConsoleColor.Blue;
        Console.ForegroundColor = ConsoleColor.Red;
        Console.WriteLine(msg);
        Console.ResetColor();

    }

    public static void WriteGreenLine(String msg)
    {


        //Console.BackgroundColor = ConsoleColor.Blue;
        Console.ForegroundColor = ConsoleColor.Green;
        Console.WriteLine(msg);
        Console.ResetColor();

    }

    public static void WriteBlueLine(String msg)
    {


        //Console.BackgroundColor = ConsoleColor.Blue;
        Console.ForegroundColor = ConsoleColor.Blue;
        Console.WriteLine(msg);
        Console.ResetColor();

    }


    //  SCardContext ctx;
    // IsoReader isoReader;
    WinSCard scard = new WinSCard();

    public void Start()
    {
        _listener = new HttpListener();
        _listener.Prefixes.Add("http://*:" + Port.ToString() + "/");
        _listener.Start();
        Receive();

        /*
        apdu_select = new CommandApdu(IsoCase.Case3Short, PCSC.SCardProtocol.T1);

        apdu_select.CLA = 0x00;
        apdu_select.INS = 0xA4;
        apdu_select.P1 = 0x04;
        apdu_select.P2 = 0x00;

        apdu_select.Data = new byte[] {0x50 ,0x4B, 0x49, 0x41, 0x50, 0x50, 0x4C, 0x45, 0x54, 0x00 };

        */

        try
        {

            //this is error-prone
            /*
            ctx = (SCardContext)contextFactory.Establish(SCardScope.System);
            isoReader = new IsoReader(ctx, emulator_reader, SCardShareMode.Exclusive, SCardProtocol.T1, false);
           */
            scard.EstablishContext();
            scard.WaitForCardPresent();
            scard.Connect(emulator_reader);

            WriteGreenLine("connected to reader " + emulator_reader);
        }
        catch (Exception ex) {

            WriteRedLine("cannot connect to reader " + emulator_reader);
            Stop();

        }

    }

    public void Stop()
    {
        _listener.Stop();
        WriteBlueLine("APDU server stopped");
    }

    private void Receive()
    {
        
        _listener.BeginGetContext(new AsyncCallback(ListenerCallback), _listener);
    }

    private void ListenerCallback(IAsyncResult result)
    {
        if (_listener.IsListening)
        {
            var context = _listener.EndGetContext(result);
            var request = context.Request;

           // var body = new StreamReader(request.InputStream).ReadToEnd();

            // do something with the request
            Console.WriteLine($"{request.Url}");
            Console.WriteLine($"{request.RawUrl}");

            Console.WriteLine("body data content length ="+ request.ContentLength64);

            if(request.ContentLength64==-1)
            {
                Console.WriteLine("body data content length is not known");
            }

            Console.WriteLine("amount of query values="+ request.QueryString.Count);
            Console.WriteLine("user agent="+ request.UserAgent);

            //Console.WriteLine(request.QueryString);
            try
            {
                String s = request.QueryString["req"];

                if(s==null)
                {
                    goto __RECEIVE;
                }

                Console.WriteLine("req=" + s);


                s = s.Replace('-', '+'); // 62nd char of encoding
                s = s.Replace('_', '/'); // 63rd char of encoding

                switch (s.Length % 4) // Pad with trailing '='s
                {
                    case 0: break; // No pad chars in this case
                    case 2: s += "=="; break; // Two pad chars
                    case 3: s += "="; break; // One pad char
                    default: throw new Exception("Illegal base64url string!");
                }

                String command = s;

                byte[] apdu_command=Convert.FromBase64String(command);

                //CLA - INS - P1 -P2 -Lc
                
                if (apdu_command.Length < 4)
                {
                    Console.WriteLine("wrong apdu command");
                    context.Response.OutputStream.Flush();
                    context.Response.Close();

                    goto __RECEIVE;
                }
                else
                {

                    byte CLA = (byte)apdu_command[0];
                    byte INS = (byte)apdu_command[1];
                    byte P1 = (byte)apdu_command[2];
                    byte P2 = (byte)apdu_command[3];

                    byte Lc = 0x00;

                    if (apdu_command.Length > 4)
                    {
                       Lc =  (byte)apdu_command[4];
                    }
                    WriteBlueLine("sending APDU :");
                    Console.WriteLine("CLA=0x{0:X2}", CLA);
                    Console.WriteLine("INS=0x{0:X2}", INS);
                    Console.WriteLine("P1=0x{0:X2}", P1);
                    Console.WriteLine("P2=0x{0:X2}", P2);

                    //send to the applet
                    CommandApdu apdu = null;


                    if (Lc == 0)
                    {
                        apdu = new CommandApdu(IsoCase.Case1, PCSC.SCardProtocol.T1);
                    }
                    else
                    {
                        apdu = new CommandApdu(IsoCase.Case3Short, PCSC.SCardProtocol.T1);
                    }


                


                    apdu.CLA = CLA;
                    apdu.INS = INS;
                    apdu.P1 = P1;
                    apdu.P2 = P2;
                  

                    if (Lc > 0)
                    {

                       
                        if(apdu_command.Length<5+Lc)
                        {
                            WriteRedLine("wrong apdu command");
                            context.Response.OutputStream.Flush();
                            context.Response.Close();
                            goto __RECEIVE;
                        }

                        List<byte> data_ = new List<byte>();
                        WriteBlueLine("sending " + Lc + " bytes of data");
                        Console.Write("data ->:");
                        for (int i=0;i<Lc;i++)
                        {
                            
                            data_.Add((byte)apdu_command[i + 5]);

                            Console.Write(" {0:X2}", apdu_command[i+5]);
                           
                        }

                        apdu.Data = data_.ToArray();

                        Console.WriteLine("");

                    }


                    Response res;


                    //no select prior to apdu commands
                    //  res = isoReader.Transmit(apdu_select);

                    //   Console.WriteLine("Response from select : 0x{0:X2} 0x{1:X2} ", res[0].SW1, res[0].SW2);
                    //need to change to something else as it follows some weird ISO7816 rules and may create errors
                    //      res = isoReader.Transmit(apdu);


                    byte[] response = new byte[256];
                    int l_res = response.Length;


                    scard.Transmit(apdu.ToArray(), apdu.ToArray().Length, response, ref l_res);

                    WriteBlueLine("->" + BitConverter.ToString(apdu.ToArray()).Replace("-", ""));

                    String SW = "SW1 SW2 = " + BitConverter.ToString(new byte[] { response[l_res - 2], response[l_res - 1] }).Replace("-", "");


                    

                    WriteBlueLine("Receving APDU :");

                    if (l_res>2)
                    {
                        WriteBlueLine("Receving " + (l_res-2) +" bytes of data");

                        for (int b = 0; b <( l_res-2) ; b++)
                                {
                                    Console.Write(" {0:X2}", response[b]);
                                }
                                Console.WriteLine("");
                    }


                    if ((response[l_res - 2] != 0x90) && (response[l_res - 2] != 0x6C) && (response[l_res - 2] != 0x61))
                    {
                        WriteRedLine(String.Format("Response from command : 0x{0:X2} 0x{1:X2} ", response[l_res - 2], response[l_res - 1]));

                    }
                    else
                    {
                        WriteGreenLine(String.Format("Response from command : 0x{0:X2} 0x{1:X2} ", response[l_res - 2], response[l_res - 1]));
                    }

                    // }
                    //  }

                    String response_;
                   // List<byte> data = new List<byte>();

                    /*
                   
                    if( l_res>2)
                    {


                        data.AddRange(res[0].GetData());
                        
                    }
                    data.AddRange(new byte[] { res[0].SW1, res[0].SW2 });
                    */



                    s = Convert.ToBase64String(response.Take(l_res).ToArray());

                    s = s.Split('=')[0]; // Remove any trailing '='s
                    s = s.Replace('+', '-'); // 62nd char of encoding
                    s = s.Replace('/', '_'); // 63rd char of encoding

                    response_ = s;


                    Console.WriteLine("sending base64 response: -->" + response_+"<--");

                    context.Response.OutputStream.Write(Encoding.UTF8.GetBytes(response_));
                    context.Response.OutputStream.Flush();
                    context.Response.Close();
                }


            }
            catch(Exception ex)
            {
                //context.Response.OutputStream.Write(Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes(ex.Message))));
                WriteRedLine("error=" + ex.Message);
            }

            __RECEIVE:

            Receive();
        }
    }
}
