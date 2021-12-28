# Signal-BA

Signal-BA is an unofficial fully FOSS version of Signal with some extra features.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="100">](https://benarmstead.github.io/fdroid/repo?fingerprint=9CCBC7C297F0B54520834681D1D29C2184B2CD262E80AA9E453215284ED3D684)

Or install it via this [apk](https://github.com/benarmstead/Signal-BA/releases/latest).

## Installation

Signal-BA uses the original signal package name, and therefore can be installed and replace signal without having to re-setup signal or lose any data.

However this can sometimes fail, in which case you will need to clear your signal app data (in android settings), uninstall signal, and then install Signal-BA (this will result in the loss of your signal data so make sure to backup first).

## Features

- **Fully FOSS**

  The official signal client includes some proprietary google dependencies. In this fork, they are replaced the with FOSS alternatives

- **Uses Open Street Maps**
 
![signal-2021-12-28-094256_004](https://user-images.githubusercontent.com/70973680/147552915-957b8a66-3505-419f-9101-505943361790.jpeg)

  The official signal client uses google maps as its default maps provider (for sharing location). In this fork, open street maps is used instead.

- **Deleting messages time extension**

![signal-2021-12-28-094256_003](https://user-images.githubusercontent.com/70973680/147553026-3cbe194e-0d0e-485c-afac-76212e97c822.jpeg)

  Messages must be deleted within 3 hours of sending on Signal. Signal-BA has modified this to 24 hours (the limit the recipient will allow deleted messages up to).
  
- **Does not allow remote deletion on the device**

![](https://support.signal.org/hc/article_attachments/360072436632/Remote_delete-ios-cropped.png)  
  Signal allows contacts to delete messages on your device up to 3 hours after sending. This fork will ignore these deletion requests.

- **Does not compress images or video**

![signal-2021-12-28-094256_002](https://user-images.githubusercontent.com/70973680/147552996-eec388c0-3bdb-4180-aaa2-c69ac242a499.jpeg)

  This fork is modified to not compress messages or video. This means that you can now easily send images and video to friends etc, without worrying about extreme compression reducing the quality. We did however make a security trade off here. Images sent via Signal-BA **DO** contain their original metadata. It is highly recommended to send images and video **ONLY** to people you trust, as the metadata could give away things such as your location when taking the image, time you took it, phone model, etc etc to the recipient.

- **Refuses to send read receipts**

![signal-2021-12-28-094256_001](https://user-images.githubusercontent.com/70973680/147552957-e6dc3ab5-3ece-4ca4-a80f-757596f15c07.jpeg)

  This fork has been modified to not send read receipts at all. Meaning you may be able to see someone else has read your message, but not vice versa.

- **Refuses to send typing indicators**

![Typing indicators](https://support.signal.org/hc/article_attachments/360016877511/typing-animation-3x.gif)

  Same as above but for typing indicators.

# License & Legal

License and legal notices in the original [README](README-ORIG.md).
