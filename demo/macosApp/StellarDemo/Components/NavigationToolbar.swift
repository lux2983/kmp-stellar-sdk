import SwiftUI

struct NavigationToolbar: ViewModifier {
    let title: String
    let subtitle: String?
    let showBackButton: Bool
    let onBack: (() -> Void)?

    @Environment(\.dismiss) private var dismiss

    init(title: String, subtitle: String? = nil, showBackButton: Bool = true, onBack: (() -> Void)? = nil) {
        self.title = title
        self.subtitle = subtitle
        self.showBackButton = showBackButton
        self.onBack = onBack
    }

    func body(content: Content) -> some View {
        VStack(spacing: 0) {
            // Custom navigation bar (always visible)
            customNavigationBar

            // Content below the navigation bar
            content
        }
        .navigationBarBackButtonHidden(true)
    }

    private var customNavigationBar: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .center) {
                // Back button overlay (left aligned, doesn't affect centering)
                HStack {
                    if showBackButton {
                        Button(action: {
                            if let onBack = onBack {
                                onBack()
                            } else {
                                dismiss()
                            }
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: "chevron.left")
                                    .font(.system(size: 12, weight: .semibold))
                                Text("Back")
                                    .font(.system(size: 12))
                            }
                            .foregroundColor(.white)
                        }
                        .buttonStyle(.plain)
                    }
                    Spacer()
                }
                .padding(.horizontal, 12)

                // Title and subtitle (truly centered, both horizontally and vertically)
                VStack(spacing: 2) {
                    Text(title)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.white)

                    if let subtitle = subtitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                .frame(maxHeight: .infinity)
            }
            .frame(height: 24)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(red: 0.04, green: 0.31, blue: 0.84),
                        Color(red: 0.02, green: 0.22, blue: 0.64)
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .shadow(color: Color(red: 0.04, green: 0.31, blue: 0.84).opacity(0.3), radius: 4, x: 0, y: 2)

            // Divider
            Divider()
                .background(Color.white.opacity(0.2))
        }
    }
}

extension View {
    func navigationToolbar(
        title: String,
        subtitle: String? = nil,
        showBackButton: Bool = true,
        onBack: (() -> Void)? = nil
    ) -> some View {
        modifier(NavigationToolbar(
            title: title,
            subtitle: subtitle,
            showBackButton: showBackButton,
            onBack: onBack
        ))
    }
}
