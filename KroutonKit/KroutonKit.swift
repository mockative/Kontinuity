import Combine

private class KroutonSubscription: Subscription {
    private(set) var isCancelled: Bool = false

    var cancellable: Cancellation? {
        didSet {
            if isCancelled {
                cancellable?()
            }
        }
    }

    func request(_ demand: Subscribers.Demand) {
        // Not supported
    }

    func cancel() {
        isCancelled = true
        cancellable?()
    }
}

public typealias Cancellation = () -> Void

public struct KroutonPublisher<Output, Failure: Error> : Publisher {
    public typealias Receiver = (
        @escaping (Output) -> Void,
        @escaping () -> Void,
        @escaping (Failure) -> Void
    ) -> Cancellation

    private let _receive: Receiver

    public init(_ receive: @escaping Receiver) {
        self._receive = receive
    }

    public func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = KroutonSubscription()
        subscriber.receive(subscription: subscription)

        subscription.cancellable = _receive(
            { output in _ = subscriber.receive(output) },
            { subscriber.receive(completion: .finished) },
            { error in subscriber.receive(completion: .failure(error)) }
        )
    }
}

public struct KroutonFuture<Output, Failure: Error> : Publisher {
    public typealias Receiver = (
        @escaping (Output) -> Void,
        @escaping (Failure) -> Void
    ) -> Cancellation

    private let _receive: Receiver

    public init(_ receive: @escaping Receiver) {
        self._receive = receive
    }

    public func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = KroutonSubscription()
        subscriber.receive(subscription: subscription)

        subscription.cancellable = _receive(
            { output in _ = subscriber.receive(output); subscriber.receive(completion: .finished) },
            { error in subscriber.receive(completion: .failure(error)) }
        )
    }
}