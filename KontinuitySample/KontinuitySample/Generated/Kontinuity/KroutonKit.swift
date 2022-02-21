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

extension KroutonPublisher where Output == Bool {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.boolValue) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Int8 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.int8Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Int16 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.int16Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Int32 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.int32Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Int64 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.int64Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Int {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.intValue) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == UInt8 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.uint8Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == UInt16 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.uint16Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == UInt32 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.uint32Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == UInt64 {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.uint64Value) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == UInt {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.uintValue) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Float {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.floatValue) }, receiveSuccess, receiveFailure)
        }
    }
}

extension KroutonPublisher where Output == Double {
    public init(_ receive: @escaping KroutonPublisher<NSNumber, Failure>.Receiver) {
        self.init { receiveElement, receiveSuccess, receiveFailure in
            receive({ receiveElement($0.doubleValue) }, receiveSuccess, receiveFailure)
        }
    }
}

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

extension KroutonFuture where Output == Bool {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.boolValue) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Int8 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.int8Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Int16 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.int16Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Int32 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.int32Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Int64 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.int64Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Int {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.intValue) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == UInt8 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.uint8Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == UInt16 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.uint16Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == UInt32 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.uint32Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == UInt64 {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.uint64Value) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == UInt {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.uintValue) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Float {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.floatValue) }, receiveFailure)
        }
    }
}

extension KroutonFuture where Output == Double {
    public init(_ receive: @escaping KroutonFuture<NSNumber, Failure>.Receiver) {
        self.init { receiveSuccess, receiveFailure in
            receive({ receiveSuccess($0.doubleValue) }, receiveFailure)
        }
    }
}

