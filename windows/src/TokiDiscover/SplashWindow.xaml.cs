using Microsoft.UI.Windowing;
using Microsoft.UI.Xaml;
using System.Runtime.InteropServices;
using Windows.Graphics;
namespace TokiDiscover;
public sealed partial class SplashWindow : Window
{
    // Display size in DIP; the image is 1281x788 with a 35px corner radius.
    private const int DisplayWidthDip = 720;
    private const int ImageWidth = 1281, ImageHeight = 788, CornerRadius = 35;
    public SplashWindow()
    {
        InitializeComponent();
        Title = "TokiDiscover";
        // Borderless, non-resizable, always on top while it is shown.
        if (AppWindow.Presenter is OverlappedPresenter presenter)
        {
            presenter.SetBorderAndTitleBar(false, false);
            presenter.IsResizable = false;
            presenter.IsMinimizable = false;
            presenter.IsMaximizable = false;
            presenter.IsAlwaysOnTop = true;
        }
        var hwnd = WinRT.Interop.WindowNative.GetWindowHandle(this);
        var scale = GetDpiForWindow(hwnd) / 96.0;
        int w = (int)(DisplayWidthDip * scale);
        int h = (int)(DisplayWidthDip * ImageHeight / (double)ImageWidth * scale);
        var area = DisplayArea.GetFromWindowId(AppWindow.Id, DisplayAreaFallback.Primary)?.WorkArea ?? DisplayArea.Primary.WorkArea;
        AppWindow.MoveAndResize(new RectInt32(area.X + (area.Width - w) / 2, area.Y + (area.Height - h) / 2, w, h));
        // Clip the window to the image's rounded rect so the corners stay transparent.
        int r = (int)(CornerRadius * DisplayWidthDip / (double)ImageWidth * scale);
        SetWindowRgn(hwnd, CreateRoundRectRgn(0, 0, w, h, r * 2, r * 2), true);
    }
    [DllImport("user32.dll")] private static extern uint GetDpiForWindow(IntPtr hwnd);
    [DllImport("gdi32.dll")] private static extern IntPtr CreateRoundRectRgn(int x1, int y1, int x2, int y2, int w, int h);
    [DllImport("user32.dll")] private static extern int SetWindowRgn(IntPtr hWnd, IntPtr hRgn, bool redraw);
}
