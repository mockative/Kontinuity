import KontinuityCore

/// Wraps the `KontinuityFlow` in an `AsyncThrowingStream`.
/// - Parameter kontinuityFlow: The kontinuity flow to collect.
/// - Returns: An stream that yields the collected values.
public func asyncStream<Output, Failure: Error, Unit>(for kontinuityFlow: @escaping KontinuityFlow<Output, Failure, Unit>) -> AsyncThrowingStream<Output, Error> {
    let asyncStreamActor = AsyncStreamActor<Output, Unit>()
    return AsyncThrowingStream { continuation in
        continuation.onTermination = { @Sendable _ in
            Task { await asyncStreamActor.cancel() }
        }
        Task {
            let kontinuityCancellable = kontinuityFlow({ item, unit in
                continuation.yield(item)
                return unit
            }, { error, unit in
                if let error = error {
                    continuation.finish(throwing: error)
                } else {
                    continuation.finish(throwing: nil)
                }
                return unit
            })
            await asyncStreamActor.setKontinuityCancellable(kontinuityCancellable)
        }
    }
}

internal actor AsyncStreamActor<Output, Unit> {
    
    private var isCancelled = false
    private var kontinuityCancellable: KontinuityCancellable<Unit>? = nil
    
    func setKontinuityCancellable(_ kontinuityCancellable: @escaping KontinuityCancellable<Unit>) {
        guard !isCancelled else {
            _ = kontinuityCancellable()
            return
        }
        self.kontinuityCancellable = kontinuityCancellable
    }
    
    func cancel() {
        isCancelled = true
        _ = kontinuityCancellable?()
        kontinuityCancellable = nil
    }
}
