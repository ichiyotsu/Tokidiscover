namespace TokiDiscover.Core;
/// <summary>Version is read from the assembly (Directory.Build.props: 0.2.0 / InformationalVersion 0.2.0-b1), never hardcoded in UI.</summary>
public static class AppVersion
{
    public static string Display => typeof(AppVersion).Assembly.GetName().Version?.ToString() ?? "0.2.0.0";
    public static string Informational => "0.2.0-b1"; // mirrors Directory.Build.props InformationalVersion
}
