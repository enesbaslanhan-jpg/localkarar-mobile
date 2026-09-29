import SwiftUI
import UIKit
import UserNotifications
import ComposeApp

/*
 * TELEFON BİLDİRİMİ (push) — 29.09.2026.
 *
 * SwiftUI yaşam döngüsünde `didRegisterForRemoteNotificationsWithDeviceToken`
 * yalnız bir UIApplicationDelegate'e gelir; bu yüzden `@UIApplicationDelegateAdaptor`.
 * Bu sınıf Kotlin tarafına iki şey verir:
 *   - Apple'ın cihaz kodu  → PushKaydi (AppShell sunucuya kaydeder)
 *   - bildirime dokunma    → DeepLinkDispatcher (universal link ile AYNI yol)
 *
 * İzin sorusu burada DEĞİL, Kotlin'de (kendi açıklamamızla, bir kez).
 */
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
	func application(
		_ application: UIApplication,
		didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
	) -> Bool {
		UNUserNotificationCenter.current().delegate = self
		return true
	}

	func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
		let hex = deviceToken.map { String(format: "%02x", $0) }.joined()
		PushKaydi.shared.tokenAlindi(token: hex)
	}

	func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
		PushKaydi.shared.kayitBasarisiz(sebep: error.localizedDescription)
	}

	/* Uygulama AÇIKKEN gelen bildirim de üstte şerit olarak gösterilsin. */
	func userNotificationCenter(
		_ center: UNUserNotificationCenter,
		willPresent notification: UNNotification,
		withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
	) {
		completionHandler([.banner, .list, .sound])
	}

	/* Dokunma: yük içindeki `url` derin bağlantı olarak açılır. */
	func userNotificationCenter(
		_ center: UNUserNotificationCenter,
		didReceive response: UNNotificationResponse,
		withCompletionHandler completionHandler: @escaping () -> Void
	) {
		if let url = response.notification.request.content.userInfo["url"] as? String {
			_ = DeepLinkDispatcher.shared.submit(rawUrl: url)
		}
		completionHandler()
	}
}

@main
struct iOSApp: App {
	@UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
