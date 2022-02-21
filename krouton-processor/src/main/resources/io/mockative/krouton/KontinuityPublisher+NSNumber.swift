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
