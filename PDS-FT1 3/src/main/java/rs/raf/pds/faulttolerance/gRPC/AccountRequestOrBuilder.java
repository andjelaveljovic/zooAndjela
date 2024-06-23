// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: account_service.proto

// Protobuf Java Version: 3.25.2
package rs.raf.pds.faulttolerance.gRPC;

public interface AccountRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:AccountRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 request_id = 1;</code>
   * @return The requestId.
   */
  int getRequestId();

  /**
   * <code>optional float amount = 2;</code>
   * @return Whether the amount field is set.
   */
  boolean hasAmount();
  /**
   * <code>optional float amount = 2;</code>
   * @return The amount.
   */
  float getAmount();

  /**
   * <code>.AccountRequestType op_type = 3;</code>
   * @return The enum numeric value on the wire for opType.
   */
  int getOpTypeValue();
  /**
   * <code>.AccountRequestType op_type = 3;</code>
   * @return The opType.
   */
  rs.raf.pds.faulttolerance.gRPC.AccountRequestType getOpType();
}
