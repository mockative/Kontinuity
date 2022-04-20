import XCTest
import KontinuityCore
import KontinuityCombine

class PublisherTests : XCTestCase {
    private class TestValue {}
    
    func testCancellableInvoked() {
        var cancelCount = 0
        
        let kontinuityFlow: KontinuityFlow<TestValue, NSError, Void> = { _, _ in
            return { cancelCount += 1 }
        }
        
        let cancellable = createPublisher(for: kontinuityFlow)
            .sink { _ in } receiveValue: { _ in }
        
        XCTAssertEqual(cancelCount, 0, "Cancellable shouldn't be invoked yet")
        cancellable.cancel()
        XCTAssertEqual(cancelCount, 1, "Cancellable should be invoked once")
    }
    
    func testCompletionWithCorrectValues() {
        let values = [TestValue(), TestValue(), TestValue(), TestValue(), TestValue()]
        let kontinuityFlow: KontinuityFlow<TestValue, NSError, Void> = { itemCallback, completionCallback in
            for value in values {
                itemCallback(value, ())
            }
            completionCallback(nil, ())
            return { }
        }
        var completionCount = 0
        var valueCount = 0
        let cancellable = createPublisher(for: kontinuityFlow)
            .sink { completion in
                guard case .finished = completion else {
                    XCTFail("Publisher should complete without error")
                    return
                }
                completionCount += 1
            } receiveValue: { receivedValue in
                XCTAssertIdentical(receivedValue, values[valueCount], "Received incorrect value")
                valueCount += 1
            }
        _ = cancellable // This is just to remove the unused variable warning
        XCTAssertEqual(completionCount, 1, "Completion closure should be called once")
        XCTAssertEqual(valueCount, values.count, "Value closure should be called for every value")
    }
    
    func testCompletionWithError() {
        let error = NSError(domain: "Test", code: 0)
        let kontinuityFlow: KontinuityFlow<TestValue, NSError, Void> = { _, completionCallback in
            completionCallback(error, ())
            return { }
        }
        var completionCount = 0
        var valueCount = 0
        let cancellable = createPublisher(for: kontinuityFlow)
            .sink { completion in
                guard case let .failure(receivedError) = completion else {
                    XCTFail("Publisher should complete with an error")
                    return
                }
                XCTAssertIdentical(receivedError, error, "Received incorrect error")
                completionCount += 1
            } receiveValue: { _ in
                valueCount += 1
            }
        _ = cancellable // This is just to remove the unused variable warning
        XCTAssertEqual(completionCount, 1, "Completion closure should be called once")
        XCTAssertEqual(valueCount, 0, "Value closure shouldn't be called")
    }
}
