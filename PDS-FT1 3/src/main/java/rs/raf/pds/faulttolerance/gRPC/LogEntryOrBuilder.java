// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: account_service.proto

// Protobuf Java Version: 3.25.2
package rs.raf.pds.faulttolerance.gRPC;

public interface LogEntryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:LogEntry)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>uint64 entryAtIndex = 1;</code>
   * @return The entryAtIndex.
   */
  long getEntryAtIndex();

  /**
   * <code>bytes logEntryData = 2;</code>
   * @return The logEntryData.
   */
  com.google.protobuf.ByteString getLogEntryData();
}
