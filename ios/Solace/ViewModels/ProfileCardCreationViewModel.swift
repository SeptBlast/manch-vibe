import SwiftUI
import PhotosUI

@MainActor
final class ProfileCardCreationViewModel: ObservableObject {

    @Published var selectedPhoto: PhotosPickerItem? = nil
    @Published var photoImage: UIImage? = nil
    @Published var isUploading: Bool = false
    @Published var isSaved: Bool = false
    @Published var error: String? = nil

    private let userRepo = UserRepository.shared

    func loadPhoto(from item: PhotosPickerItem?) async {
        guard let item else { return }
        if let data = try? await item.loadTransferable(type: Data.self),
           let img = UIImage(data: data) {
            photoImage = img
        }
    }

    func publishCard(uid: String, answers: OnboardingAnswers) async {
        isUploading = true
        error = nil
        do {
            let avatarUrl: String?
            if let img = photoImage, let data = img.jpegData(compressionQuality: 0.8) {
                avatarUrl = try await userRepo.uploadAvatar(uid: uid, imageData: data)
            } else {
                avatarUrl = nil
            }

            let profile = buildProfile(uid: uid, answers: answers, avatarUrl: avatarUrl)
            try await userRepo.saveProfile(uid: uid, profile: profile)
            isSaved = true
        } catch {
            self.error = error.localizedDescription
        }
        isUploading = false
    }

    private func buildProfile(uid: String, answers: OnboardingAnswers, avatarUrl: String?) -> UserProfile {
        let moodEntry = defaultMoodPalette.first { $0.key == answers.moodColor }
        return UserProfile(
            id: uid,
            username: answers.username.isEmpty ? "Anonymous" : answers.username,
            avatarUrl: avatarUrl,
            moodColor: answers.moodColor,
            moodLabel: moodEntry?.label ?? answers.moodColor.rawValue.replacingOccurrences(of: "_", with: " ").capitalized,
            bio: answers.feelingSentence,
            vibes: answers.vibes
        )
    }
}
