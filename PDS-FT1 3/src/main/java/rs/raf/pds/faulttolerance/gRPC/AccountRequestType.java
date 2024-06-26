// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: account_service.proto

// Protobuf Java Version: 3.25.2
package rs.raf.pds.faulttolerance.gRPC;

/**
 * Protobuf enum {@code AccountRequestType}
 */
public enum AccountRequestType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>GET = 0;</code>
   */
  GET(0),
  /**
   * <code>ADD = 1;</code>
   */
  ADD(1),
  /**
   * <code>WITDRAWAL = 2;</code>
   */
  WITDRAWAL(2),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>GET = 0;</code>
   */
  public static final int GET_VALUE = 0;
  /**
   * <code>ADD = 1;</code>
   */
  public static final int ADD_VALUE = 1;
  /**
   * <code>WITDRAWAL = 2;</code>
   */
  public static final int WITDRAWAL_VALUE = 2;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static AccountRequestType valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static AccountRequestType forNumber(int value) {
    switch (value) {
      case 0: return GET;
      case 1: return ADD;
      case 2: return WITDRAWAL;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<AccountRequestType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      AccountRequestType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<AccountRequestType>() {
          public AccountRequestType findValueByNumber(int number) {
            return AccountRequestType.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return rs.raf.pds.faulttolerance.gRPC.AccountServiceOuterClass.getDescriptor().getEnumTypes().get(0);
  }

  private static final AccountRequestType[] VALUES = values();

  public static AccountRequestType valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private AccountRequestType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:AccountRequestType)
}

