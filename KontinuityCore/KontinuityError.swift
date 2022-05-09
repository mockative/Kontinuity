import Foundation

extension Error {
    /**
     Returns the original exception thrown in Kotlin, typically an instance of `KotlinThrowable`.
     The return type of this property is `Any` since KontinuityCore does not know about the types
     of your Kotlin module.
     
     If this error is not an error thrown from Kotlin, this propery returns `nil`.
     */
    public var kotlinException: Any? {
        (self as NSError).userInfo["KotlinException"]
    }
}
