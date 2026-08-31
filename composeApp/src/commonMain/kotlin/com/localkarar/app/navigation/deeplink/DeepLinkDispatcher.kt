package com.localkarar.app.navigation.deeplink

object DeepLinkDispatcher {

    fun submit(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) return false

        return when (val result = DeepLinkParser.parse(rawUrl)) {
            is DeepLinkResult.Success -> {
                PendingDeepLinkStore.set(result.target)
                true
            }
            is DeepLinkResult.Unsupported -> {
                println("[DeepLink] Unsupported deep link: $rawUrl")
                false
            }
            is DeepLinkResult.Malformed -> {
                println("[DeepLink] Malformed deep link: $rawUrl (${result.reason})")
                false
            }
        }
    }
}
