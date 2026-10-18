#if canImport(Testing)
import Testing
import Criterion

@Suite("Criterion Swift Export Tests")
struct CriterionExportTests {
    @Test("Criterion swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Criterion swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Criterion

final class CriterionExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Criterion swift module imported cleanly")
    }
}
#endif
