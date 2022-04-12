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