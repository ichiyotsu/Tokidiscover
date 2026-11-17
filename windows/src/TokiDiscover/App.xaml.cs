using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using System;
namespace TokiDiscover;
public partial class App : Application
{
    public static MainWindow? Window { get; private set; }
    public static Services.BrowserService Browser { get; } = new();
    private SplashWindow? _splash;
    private DispatcherTimer? _splashTimer;
    private DateTime _splashShownAt;
    private bool _mainActivated;
    public App() => InitializeComponent();
    protected override void OnLaunched(LaunchActivatedEventArgs args)
    {
        _splash = new SplashWindow();
        _splashShownAt = DateTime.Now;
        _splash.Activate();
        Window = new MainWindow();
        Window.Activated += (_, _) => _mainActivated = true;
        Window.Activate();
        // Close the splash once the main window is up and the splash has shown
        // for at least 800ms; never leave it up longer than 6s.
        _splashTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(100) };
        _splashTimer.Tick += (_, _) =>
        {
            var elapsed = DateTime.Now - _splashShownAt;
            if ((_mainActivated && elapsed >= TimeSpan.FromMilliseconds(800)) || elapsed >= TimeSpan.FromSeconds(6))
            {
                _splashTimer!.Stop();
                _splash?.Close();
                _splash = null;
            }
        };
        _splashTimer.Start();
    }
}
