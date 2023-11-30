
using System;
using System.Net;
using System.IO;
using System.Text;
using PCSC.Iso7816;
using PCSC;

public class HttpServer
{
    public int Port = 38099;

    private HttpListener _listener;
    IContextFactory contextFactory = ContextFactory.Instance;
    //string emulator_reader="JAVACOS Virtual Contact Reader 0";
    string emulator_reader = "Identive SCR33xx v2.0 USB SC Reader 0";
    CommandApdu apdu_select;

    public void Start()
    {
        _listener = new HttpListener();
        _listener.Prefixes.Add("http://*:" + Port.ToString() + "/");
        _listener.Start();
        Receive();

        apdu_select = new CommandApdu(IsoCase.Case3Short, PCSC.SCardProtocol.T1);

        apdu_select.CLA = 0x00;
        apdu_select.INS = 0xA4;
        apdu_select.P1 = 0x04;
        apdu_select.P2 = 0x00;

        apdu_select.Data = new byte[] {0x50 ,0x4B, 0x49, 0x41, 0x50, 0x50, 0x4C, 0x45, 0x54, 0x00 };
       
    }

    public void Stop()
    {
        _listener.Stop();
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

                char[] apdu_command=Encoding.UTF8.GetChars(Convert.FromBase64String(command));

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

                    if(INS==0x44)
                    {
                        apdu = new CommandApdu(IsoCase.Case4Short, PCSC.SCardProtocol.T1);
                        apdu.Le = 0x08;
                    }

                


                apdu.CLA = CLA;
                    apdu.INS = INS;
                    apdu.P1 = P1;
                    apdu.P2 = P2;
                  

                    if (Lc > 0)
                    {

                       
                        if(apdu_command.Length<5+Lc)
                        {
                            Console.WriteLine("wrong apdu command");
                            context.Response.OutputStream.Flush();
                            context.Response.Close();
                            goto __RECEIVE;
                        }

                        List<byte> data_ = new List<byte>();
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

                    using (var ctx = contextFactory.Establish(SCardScope.System))
                    {

                        using (var isoReader = new IsoReader(ctx, emulator_reader, SCardShareMode.Shared, SCardProtocol.Any, false))
                        {
                            res = isoReader.Transmit(apdu_select);

                            Console.WriteLine("Response from select : 0x{0:X2} 0x{1:X2} ", res[0].SW1, res[0].SW2);

                            res = isoReader.Transmit(apdu);
                            if (res[0].GetData() != null)
                            {
                                for (int b = 0; b < res[0].GetData().Count(); b++)
                                {
                                    Console.Write(" {0:X2}", res[0].GetData()[b]);
                                }
                                Console.WriteLine("");
                            }


                            Console.WriteLine("Response from command : 0x{0:X2} 0x{1:X2} ", res[0].SW1, res[0].SW2);
                        }
                    }

                    String response;
                    List<byte> data = new List<byte>();

                    data.AddRange(new byte[] { 0xFF, 0xFF, res[0].SW1, res[0].SW2 });

                    if (res[0].GetData() != null)
                    {


                        data.AddRange(res[0].GetData());
                        
                    }



                    s=Convert.ToBase64String(data.ToArray());

                    s = s.Split('=')[0]; // Remove any trailing '='s
                    s = s.Replace('+', '-'); // 62nd char of encoding
                    s = s.Replace('/', '_'); // 63rd char of encoding

                    response = s;


                    Console.WriteLine("sending base64 response: -->" + response+"<--");

                    context.Response.OutputStream.Write(Encoding.UTF8.GetBytes(response));
                    context.Response.OutputStream.Flush();
                    context.Response.Close();
                }


            }
            catch(Exception ex)
            {
                //context.Response.OutputStream.Write(Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes(ex.Message))));
                Console.WriteLine("error=" + ex.Message);
            }

            __RECEIVE:

            Receive();
        }
    }
}
