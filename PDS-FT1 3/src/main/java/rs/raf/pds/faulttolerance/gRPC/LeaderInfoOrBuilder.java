// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: account_service.proto

// Protobuf Java Version: 3.25.2
package rs.raf.pds.faulttolerance.gRPC;

public interface LeaderInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:LeaderInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool imLeader = 1;</code>
   * @return The imLeader.
   */
  boolean getImLeader();

  /**
   * <code>string hostnamePort = 2;</code>
   * @return The hostnamePort.
   */
  java.lang.String getHostnamePort();
  /**
   * <code>string hostnamePort = 2;</code>
   * @return The bytes for hostnamePort.
   */
  com.google.protobuf.ByteString
      getHostnamePortBytes();
}
