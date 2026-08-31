# M4 Community 2.0 — Web ↔ Mobile Parity Matrix

## Authoritative Web Source
- **Repository:** `https://github.com/enesbaslanhan-jpg/local_akademi`
- **Branch:** `design/localkarar-18`
- **Verified HEAD:** `f3f1711a8aa35abed345816cf9492c6ba46b0aa6`

---

## Parity Classification Table

| Feature | Canonical Endpoint | HTTP Method | Web Behavior | Mobile Native Parity | Classification |
|---|---|---|---|---|---|
| **Feed** | `/community` | `GET` | Cursor-based paginated feed with filters (Tümü, Resmi, Topluluk) | Native LazyColumn with pull/load-more and filter pills | **ALIGNED** |
| **Post Detail** | `/community/post/:postId` | `GET` | Displays post, author, media, quoted post, parent post context, and nested replies tree (depth 3) | Full detail screen with parent card, quoted post, and hierarchical recursive reply view | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Create Post** | `/community/posts` | `POST` | Direct publishing (`status: 'published'`) with max 500 chars `metin` | Native compose sheet with character counter and immediate publication feedback | **ALIGNED** |
| **Media** | `/community/media` | `POST`/`DELETE` | Multipart `file` upload (PNG, JPEG, MP4, WebM, PDF, DOCX <= 20MB) | Native platform file picker, preview, attachment pill, and discard support | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Like** | `/community/:postId/like` | `POST`/`DELETE` | Toggle like status, updates count, sends notification | Optimistic UI toggle with rollback and counter update | **ALIGNED** |
| **Reply** | `/community/posts` | `POST` | Creates post with `parentId`, validates published target | Context banner in composer, replies hierarchy in detail | **ALIGNED** |
| **Quote** | `/community/posts` | `POST` | Creates post with `quotedPostId`, renders embedded quote card | Embedded quoted post preview in feed and detail | **ALIGNED** |
| **Bookmark** | `/community/:postId/bookmark` | `POST`/`DELETE` | Personal bookmarking, private to user | Bookmark button toggle with feed and detail persistence | **ALIGNED** |
| **Delete** | `/community/:postId` | `DELETE` | Soft-deletes post by author or admin | Delete button on own posts with confirm dialog | **ALIGNED** |
| **Post Report** | `/community/:postId/reports` | `POST` | Post reporting with reasons (spam, harassment, misinformation, etc.) | Native radio selection dialog with custom explanation | **ALIGNED** |
| **People** | `/community/social/people?q=` | `GET` | User directory with search query, returns `followingIds` and `blockedIds` | Searchable list with instant search and follow/block actions | **ALIGNED** |
| **Follow** | `/community/social/people/:id/follow` | `POST`/`DELETE` | Follow/unfollow, syncs `followingIds` | Instant toggle with optimistic state and rollback | **ALIGNED** |
| **Block** | `/community/social/people/:id/block` | `POST`/`DELETE` | Mutual block, removes mutual follows | Instant block/unblock action with follow removal | **ALIGNED** |
| **Own Profile** | `/community/me/summary`<br>`/community/me/:liste` | `GET` | Summary stats (paylasim, begeni, kayit, takipci, takipEdilen) + tabs (Paylaşımlarım, Beğenilerim, Kaydettiklerim) | Native profile view with stat counters and tab switching | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Other Profile** | `/community/social/people/:userId/profile`<br>`/community/people/:userId/posts` | `GET` | Profile identity (name, role, bio, location, website), stat counters, follow/block state, posts and media filter | Native profile card with external URL launcher and posts/media tabs | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Followers** | `/community/social/people/:userId/followers` | `GET` | List of followers with user identity and bio | Native followers list screen with profile navigation | **ALIGNED** |
| **Following** | `/community/social/people/:userId/following` | `GET` | List of followed users with identity and bio | Native following list screen with profile navigation | **ALIGNED** |
| **Threads** | `/community/social/threads` | `GET` | Lists conversations separated by joined and invited status | Native thread cards with last message preview and invitation alerts | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Direct Thread** | `/community/social/threads` | `POST` | Initiates 1-on-1 thread with target user ID | Native user selection modal | **ALIGNED** |
| **Group Thread** | `/community/social/threads` | `POST` | Initiates multi-member thread with custom group name | Multi-select member sheet with optional group title | **ALIGNED** |
| **Invitations** | `/community/social/threads/:id/invite/:karar` | `POST` | Accept or decline group invitation | Invitation cards with Kabul Et / Reddet buttons | **ALIGNED** |
| **Messages** | `/community/social/threads/:id/messages` | `GET`/`POST` | Chronological messages with sender identity | Message bubbles with alignment (me vs other) and sticky composer | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Notifications** | `/community/social/notifications` | `GET` | Social notifications (follow, like, reply, quote, message, thread_invite) | Notification list with type icons and target navigation | **ALIGNED_WITH_NATIVE_ADAPTATION** |
| **Unread** | `/community/social/notifications`<br>`/community/social/notifications/read` | `GET`/`POST` | Unread count calculation and mark-all-read endpoint | Badge on top-bar notification bell and mark-all-read button | **ALIGNED** |
| **User Report** | `/community/social/people/:id/report` | `POST` | User reporting with categories (harassment, spam, impersonation, unsafe, other) | Dedicated user report dialog with custom reason input | **ALIGNED** |

---

## ID Types Contract Verification

| Entity | ID Type in Schema / API | Mobile Implementation | Status |
|---|---|---|---|
| User | `Int` (autoincrement) | `Int` (`CommunityAuthorDto.id: Int`, `PersonDto.id: Int`) | ✅ Verified |
| Post | `String` (UUID) | `String` (`CommunityPostDto.id: String`) | ✅ Verified |
| Thread | `String` (UUID) | `String` (`CommunityThreadDto.id: String`) | ✅ Verified |
| Message | `String` (UUID) | `String` (`ThreadMessageDto.id: String`) | ✅ Verified |
| Media | `String` (UUID) | `String` (`CommunityMediaDto.id: String`) | ✅ Verified |
| Notification | `String` (UUID) | `String` (`CommunityNotificationDto.id: String`) | ✅ Verified |

---

## Build Verification
- **Command:** `gradlew clean :composeApp:assembleDebug`
- **Result:** `BUILD SUCCESSFUL` (APK generated at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`)
- **Status:** PASS
