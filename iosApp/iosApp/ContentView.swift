import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Tam ekran: SwiftUI temsilciyi guvenli alana sikistiriyordu → ustte/altta
            // siyah seritler (TestFlight 106, 18.09.2026). Compose kenar boslugunu
            // WindowInsets ile kendi uygular; klavyeyi de kendi yonetir.
            .ignoresSafeArea()
            .onOpenURL { url in
                _ = DeepLinkDispatcher.shared.submit(rawUrl: url.absoluteString)
            }
            .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { userActivity in
                if let url = userActivity.webpageURL {
                    _ = DeepLinkDispatcher.shared.submit(rawUrl: url.absoluteString)
                }
            }
    }
}
