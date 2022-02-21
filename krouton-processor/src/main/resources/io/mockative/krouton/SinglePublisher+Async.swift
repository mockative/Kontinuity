extension SinglePublisher {
    public var value: Output {
        get async throws {
            try await withCheckedThrowingContinuation { continuation in
                var cancellable: AnyCancellable? = nil

                cancellable = sink { result in
                    switch result {
                    case .failure(let error):
                        continuation.resume(throwing: error)
                    case .success(let value):
                        continuation.resume(returning: value)
                    }

                    cancellable?.cancel()
                }
            }
        }
    }
}

extension SinglePublisher where Failure == Never {
    public var value: Output {
        get async {
            await withCheckedContinuation { continuation in
                var cancellable: AnyCancellable? = nil

                cancellable = sink(receiveValue: { value in
                    continuation.resume(returning: value)
                    cancellable?.cancel()
                })
            }
        }
    }
}
