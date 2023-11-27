class Program
{
    private static bool _keepRunning = true;

    static void Main(string[] args)
    {
        Console.CancelKeyPress += delegate (object sender, ConsoleCancelEventArgs e)
        {
            e.Cancel = true;
            Program._keepRunning = false;
        };

        Console.WriteLine("Starting  APDU server ...");

        var httpServer = new HttpServer();
        httpServer.Start();

        while (Program._keepRunning) { }

        httpServer.Stop();

        Console.WriteLine("Exiting gracefully...");
    }
}