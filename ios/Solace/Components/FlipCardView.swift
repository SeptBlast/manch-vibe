import SwiftUI

enum CardFace { case front, back }

/// 3-D Y-axis card flip. Tap anywhere to toggle sides.
struct FlipCardView<Front: View, Back: View>: View {
    @Binding var face: CardFace
    let front: () -> Front
    let back:  () -> Back

    @State private var rotation: Double = 0

    var body: some View {
        ZStack {
            front()
                .opacity(rotation < 90 ? 1 : 0)

            back()
                .rotation3DEffect(.degrees(180), axis: (x: 0, y: 1, z: 0))
                .opacity(rotation >= 90 ? 1 : 0)
        }
        .rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.5)
        .animation(.spring(response: 0.45, dampingFraction: 0.8), value: rotation)
        .onTapGesture { flip() }
        .onChange(of: face) { newFace in
            withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                rotation = newFace == .front ? 0 : 180
            }
        }
        .onAppear {
            rotation = face == .front ? 0 : 180
        }
    }

    private func flip() {
        let next: CardFace = face == .front ? .back : .front
        face = next
        withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
            rotation = next == .front ? 0 : 180
        }
    }
}
