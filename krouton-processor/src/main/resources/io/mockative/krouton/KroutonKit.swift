import Foundation
import Combine

// MARK: SinglePublisher
public protocol SinglePublisher: Publisher {
    func sink(receiveResult: @escaping (Result<Output, Failure>) -> Void) -> AnyCancellable
}

extension SinglePublisher {
    public func sink(receiveResult: @escaping (Result<Output, Failure>) -> Void) -> AnyCancellable {
        var output: Output? = nil

        return sink { completion in
            switch completion {
            case .failure(let error):
                receiveResult(.failure(error))
            case .finished:
                receiveResult(.success(output!))
            }
        } receiveValue: { value in
            output = value
        }
    }
}

%file:Publisher+Async.swift%
%file:SinglePublisher+Async.swift%
// MARK: KroutonSubscription
public typealias Cancellation = () -> Void

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

// MARK: KroutonPublisher
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

%file:KontinuityPublisher+NSNumber.swift%
// MARK: KroutonFuture
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

extension KroutonFuture where Output == Void {
    public typealias VoidReceiver = (
        @escaping () -> Void,
        @escaping (Failure) -> Void
    ) -> Cancellation

    public init(_ receive: @escaping VoidReceiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess(()) }, receiveFailure)
        }
    }
}

extension KroutonFuture: SinglePublisher {}

%file:KontinuityFuture+NSNumber.swift%
