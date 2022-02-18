extension Publisher where Failure == Never {
    public var values: AsyncStream<Output> {
        AsyncStream<Output> { continuation in
            var cancellable: AnyCancellable? = nil

            cancellable = sink { completion in
                continuation.finish()
                cancellable?.cancel()
            } receiveValue: { value in
                continuation.yield(value)
            }
        }
    }
}

extension Publisher {
    public var values: AsyncThrowingStream<Output, Error> {
        AsyncThrowingStream<Output, Error> { continuation in
            var cancellable: AnyCancellable? = nil

            cancellable = sink { completion in
                switch completion {
                case .failure(let error):
                    continuation.finish(throwing: error)
                case .finished:
                    continuation.finish()
                }

                cancellable?.cancel()
            } receiveValue: { value in
                continuation.yield(value)
            }
        }
    }
}
