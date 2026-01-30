package com.soneso.stellar.sdk.unitTests.contract

import com.soneso.stellar.sdk.contract.ContractSpec
import com.soneso.stellar.sdk.contract.NativeUnionVal
import com.soneso.stellar.sdk.contract.exception.ContractSpecException
import com.soneso.stellar.sdk.xdr.*
import kotlin.test.*

class ContractSpecCoverageTest {
    private fun typeDef(disc: SCSpecTypeXdr) = SCSpecTypeDefXdr.Void(disc)
    private val u32Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_U32)
    private val i32Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_I32)
    private val u64Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_U64)
    private val boolType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_BOOL)
    private val stringType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_STRING)
    private val symbolType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_SYMBOL)
    private val bytesType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_BYTES)
    private val addressType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_ADDRESS)
    private val timepointType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_TIMEPOINT)
    private val durationType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_DURATION)
    private val u128Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_U128)
    private val i128Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_I128)
    private val u256Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_U256)
    private val i256Type = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_I256)
    private val voidType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_VOID)
    private val errorType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_ERROR)
    private val valType = typeDef(SCSpecTypeXdr.SC_SPEC_TYPE_VAL)
    private fun buildSpec(entries: List<SCSpecEntryXdr>) = ContractSpec(entries)
    private fun funcEntry(name: String, inputs: List<SCSpecFunctionInputV0Xdr> = emptyList(), outputs: List<SCSpecTypeDefXdr> = emptyList()) =
        SCSpecEntryXdr.FunctionV0(SCSpecFunctionV0Xdr(doc = "", name = SCSymbolXdr(name), inputs = inputs, outputs = outputs))
    private fun input(name: String, type: SCSpecTypeDefXdr) = SCSpecFunctionInputV0Xdr(doc = "", name = name, type = type)

    @Test fun testScValToNative_void() { assertNull(buildSpec(emptyList()).scValToNative(SCValXdr.Void(SCValTypeXdr.SCV_VOID), null)) }
    @Test fun testScValToNative_bool() { assertEquals(true, buildSpec(emptyList()).scValToNative(SCValXdr.B(true), null)) }
    @Test fun testScValToNative_u32() { assertEquals(123u, buildSpec(emptyList()).scValToNative(SCValXdr.U32(Uint32Xdr(123u)), null)) }
    @Test fun testScValToNative_i32() { assertEquals(-42, buildSpec(emptyList()).scValToNative(SCValXdr.I32(Int32Xdr(-42)), null)) }
    @Test fun testScValToNative_u64() { assertEquals(999UL, buildSpec(emptyList()).scValToNative(SCValXdr.U64(Uint64Xdr(999UL)), null)) }
    @Test fun testScValToNative_i64() { assertEquals(-999L, buildSpec(emptyList()).scValToNative(SCValXdr.I64(Int64Xdr(-999L)), null)) }
    @Test fun testScValToNative_string() { assertEquals("hello", buildSpec(emptyList()).scValToNative(SCValXdr.Str(SCStringXdr("hello")), null)) }
    @Test fun testScValToNative_symbol() { assertEquals("sym", buildSpec(emptyList()).scValToNative(SCValXdr.Sym(SCSymbolXdr("sym")), null)) }
    @Test fun testScValToNative_bytes() { assertContentEquals(byteArrayOf(1,2,3), buildSpec(emptyList()).scValToNative(SCValXdr.Bytes(SCBytesXdr(byteArrayOf(1,2,3))), null) as ByteArray) }
    @Test fun testScValToNative_timepoint() { assertEquals(123UL, buildSpec(emptyList()).scValToNative(SCValXdr.Timepoint(TimePointXdr(Uint64Xdr(123UL))), null)) }
    @Test fun testScValToNative_duration() { assertEquals(456UL, buildSpec(emptyList()).scValToNative(SCValXdr.Duration(DurationXdr(Uint64Xdr(456UL))), null)) }
    @Test fun testScValToNative_error() { assertTrue(buildSpec(emptyList()).scValToNative(SCValXdr.Error(SCErrorXdr.ContractCode(Uint32Xdr(1u))), null) is SCErrorXdr) }
    @Test fun testScValToNative_instance() { assertTrue(buildSpec(emptyList()).scValToNative(SCValXdr.Instance(SCContractInstanceXdr(ContractExecutableXdr.Void, null)), null) is SCContractInstanceXdr) }
    @Test fun testScValToNative_vec() { val r = buildSpec(emptyList()).scValToNative(SCValXdr.Vec(SCVecXdr(listOf(SCValXdr.U32(Uint32Xdr(10u))))), SCSpecTypeDefXdr.Vec(SCSpecTypeVecXdr(u32Type))) as List<*>; assertEquals(10u, r[0]) }
    @Test fun testScValToNative_vecBadType() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).scValToNative(SCValXdr.Vec(SCVecXdr(emptyList())), boolType) } }
    @Test fun testScValToNative_map() { val r = buildSpec(emptyList()).scValToNative(SCValXdr.Map(SCMapXdr(listOf(SCMapEntryXdr(SCValXdr.Sym(SCSymbolXdr("k")), SCValXdr.U32(Uint32Xdr(1u)))))), SCSpecTypeDefXdr.Map(SCSpecTypeMapXdr(symbolType, u32Type))) as List<*>; assertEquals("k", (r[0] as Pair<*,*>).first) }
    @Test fun testScValToNative_mapBadType() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).scValToNative(SCValXdr.Map(SCMapXdr(emptyList())), boolType) } }

    @Test fun testNativeToXdr_null() { assertEquals(SCValTypeXdr.SCV_VOID, buildSpec(emptyList()).nativeToXdrSCVal(null, voidType).discriminant) }
    @Test fun testNativeToXdr_passthrough() { val o = SCValXdr.U32(Uint32Xdr(42u)); assertSame(o, buildSpec(emptyList()).nativeToXdrSCVal(o, voidType)) }
    @Test fun testNativeToXdr_boolBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("x", boolType) } }
    @Test fun testNativeToXdr_stringBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(1, stringType) } }
    @Test fun testNativeToXdr_symbolBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(1, symbolType) } }
    @Test fun testNativeToXdr_u32Range() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, u32Type) } }
    @Test fun testNativeToXdr_i32Range() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(Long.MAX_VALUE, i32Type) } }
    @Test fun testNativeToXdr_u64Neg() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, u64Type) } }
    @Test fun testNativeToXdr_timepoint() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(1, timepointType) is SCValXdr.Timepoint) }
    @Test fun testNativeToXdr_timepointNeg() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, timepointType) } }
    @Test fun testNativeToXdr_duration() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(1, durationType) is SCValXdr.Duration) }
    @Test fun testNativeToXdr_durationNeg() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, durationType) } }
    @Test fun testNativeToXdr_u128Neg() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, u128Type) } }
    @Test fun testNativeToXdr_i128() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42, i128Type) is SCValXdr.I128) }
    @Test fun testNativeToXdr_u256Neg() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(-1, u256Type) } }
    @Test fun testNativeToXdr_i256() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42, i256Type) is SCValXdr.I256) }
    @Test fun testNativeToXdr_bytesHex() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal("0102", bytesType) is SCValXdr.Bytes) }
    @Test fun testNativeToXdr_bytesList() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(listOf<Byte>(1,2), bytesType) is SCValXdr.Bytes) }
    @Test fun testNativeToXdr_bytesBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(42, bytesType) } }
    @Test fun testNativeToXdr_addrBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(42, addressType) } }
    @Test fun testNativeToXdr_addrInvalid() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("bad", addressType) } }
    @Test fun testNativeToXdr_optionNull() { assertEquals(SCValTypeXdr.SCV_VOID, buildSpec(emptyList()).nativeToXdrSCVal(null, SCSpecTypeDefXdr.Option(SCSpecTypeOptionXdr(u32Type))).discriminant) }
    @Test fun testNativeToXdr_optionVal() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42, SCSpecTypeDefXdr.Option(SCSpecTypeOptionXdr(u32Type))) is SCValXdr.U32) }
    @Test fun testNativeToXdr_vecBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("x", SCSpecTypeDefXdr.Vec(SCSpecTypeVecXdr(u32Type))) } }
    @Test fun testNativeToXdr_mapBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("x", SCSpecTypeDefXdr.Map(SCSpecTypeMapXdr(u32Type, u32Type))) } }
    @Test fun testNativeToXdr_tupleBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("x", SCSpecTypeDefXdr.Tuple(SCSpecTypeTupleXdr(listOf(u32Type)))) } }
    @Test fun testNativeToXdr_tupleMismatch() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(listOf(1), SCSpecTypeDefXdr.Tuple(SCSpecTypeTupleXdr(listOf(u32Type, u32Type)))) } }
    @Test fun testNativeToXdr_tupleOk() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(listOf(1,2), SCSpecTypeDefXdr.Tuple(SCSpecTypeTupleXdr(listOf(u32Type, u32Type)))) is SCValXdr.Vec) }
    @Test fun testNativeToXdr_bytesNOk() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(byteArrayOf(1,2,3), SCSpecTypeDefXdr.BytesN(SCSpecTypeBytesNXdr(Uint32Xdr(3u)))) is SCValXdr.Bytes) }
    @Test fun testNativeToXdr_bytesNMismatch() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(byteArrayOf(1,2), SCSpecTypeDefXdr.BytesN(SCSpecTypeBytesNXdr(Uint32Xdr(3u)))) } }
    @Test fun testNativeToXdr_bytesNBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(42, SCSpecTypeDefXdr.BytesN(SCSpecTypeBytesNXdr(Uint32Xdr(3u)))) } }
    @Test fun testNativeToXdr_errorXdr() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(SCErrorXdr.ContractCode(Uint32Xdr(5u)), errorType) is SCValXdr.Error) }
    @Test fun testNativeToXdr_errorNum() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42, errorType) is SCValXdr.Error) }
    @Test fun testNativeToXdr_errorBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("x", errorType) } }
    @Test fun testNativeToXdr_valStr() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal("hi", valType) is SCValXdr.Str) }
    @Test fun testNativeToXdr_valBool() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(true, valType) is SCValXdr.B) }
    @Test fun testNativeToXdr_valInt() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42, valType) is SCValXdr.I32) }
    @Test fun testNativeToXdr_valLong() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42L, valType) is SCValXdr.I64) }
    @Test fun testNativeToXdr_valUInt() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42u, valType) is SCValXdr.U32) }
    @Test fun testNativeToXdr_valULong() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42UL, valType) is SCValXdr.U64) }
    @Test fun testNativeToXdr_valBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(listOf(1), valType) } }
    @Test fun testNativeToXdr_resultOk() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(mapOf("ok" to 42), SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, errorType))) is SCValXdr.U32) }
    @Test fun testNativeToXdr_resultErr() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(mapOf("error" to 1), SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, u32Type))) is SCValXdr.U32) }
    @Test fun testNativeToXdr_resultNoKey() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(mapOf("x" to 1), SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, u32Type))) } }
    @Test fun testNativeToXdr_resultBad() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("bad", SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, u32Type))) } }
    @Test fun testNativeToXdr_resultOkNull() { assertEquals(SCValTypeXdr.SCV_VOID, buildSpec(emptyList()).nativeToXdrSCVal(mapOf("ok" to null), SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(voidType, errorType))).discriminant) }
    @Test fun testNativeToXdr_resultErrNull() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(mapOf("error" to null), SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, u32Type))) } }
    @Test fun testNativeToXdr_u32UInt() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42u, u32Type) is SCValXdr.U32) }
    @Test fun testNativeToXdr_u32ULong() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42UL, u32Type) is SCValXdr.U32) }
    @Test fun testNativeToXdr_u32Double() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42.0, u32Type) is SCValXdr.U32) }
    @Test fun testNativeToXdr_u32Float() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal(42.0f, u32Type) is SCValXdr.U32) }
    @Test fun testNativeToXdr_u32Str() { assertTrue(buildSpec(emptyList()).nativeToXdrSCVal("42", u32Type) is SCValXdr.U32) }
    @Test fun testNativeToXdr_u32BadStr() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal("xyz", u32Type) } }
    @Test fun testNativeToXdr_intBadType() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(listOf(1), u32Type) } }

    @Test fun testFuncResToNative_void() { assertNull(buildSpec(listOf(funcEntry("f"))).funcResToNative("f", SCValXdr.Void(SCValTypeXdr.SCV_VOID))) }
    @Test fun testFuncResToNative_voidNonVoid() { assertFailsWith<ContractSpecException> { buildSpec(listOf(funcEntry("f"))).funcResToNative("f", SCValXdr.U32(Uint32Xdr(1u))) } }
    @Test fun testFuncResToNative_notFound() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).funcResToNative("x", SCValXdr.Void(SCValTypeXdr.SCV_VOID)) } }
    @Test fun testFuncResToNative_multiOut() { assertFailsWith<ContractSpecException> { buildSpec(listOf(funcEntry("f", outputs = listOf(u32Type, u32Type)))).funcResToNative("f", SCValXdr.U32(Uint32Xdr(1u))) } }
    @Test fun testFuncResToNative_result() { assertEquals(42u, buildSpec(listOf(funcEntry("f", outputs = listOf(SCSpecTypeDefXdr.Result(SCSpecTypeResultXdr(u32Type, u32Type)))))).funcResToNative("f", SCValXdr.U32(Uint32Xdr(42u)))) }
    @Test fun testFuncResToNative_base64() { val s = buildSpec(listOf(funcEntry("f", outputs = listOf(u32Type)))); assertEquals(42u, s.funcResToNative("f", SCValXdr.U32(Uint32Xdr(42u)).toXdrBase64())) }
    @Test fun testFuncArgs_missing() { assertFailsWith<ContractSpecException> { buildSpec(listOf(funcEntry("f", inputs = listOf(input("a", u32Type), input("b", u32Type))))).funcArgsToXdrSCValues("f", mapOf("a" to 1)) } }

    @Test fun testUdtStructs() { assertEquals(1, buildSpec(listOf(SCSpecEntryXdr.UdtStructV0(SCSpecUDTStructV0Xdr("","","S",emptyList())))).udtStructs().size) }
    @Test fun testUdtUnions() { assertEquals(1, buildSpec(listOf(SCSpecEntryXdr.UdtUnionV0(SCSpecUDTUnionV0Xdr("","","U",emptyList())))).udtUnions().size) }
    @Test fun testUdtEnums() { assertEquals(1, buildSpec(listOf(SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","E",emptyList())))).udtEnums().size) }
    @Test fun testUdtErrorEnums() { assertEquals(1, buildSpec(listOf(SCSpecEntryXdr.UdtErrorEnumV0(SCSpecUDTErrorEnumV0Xdr("","","EE",emptyList())))).udtErrorEnums().size) }
    @Test fun testEvents() { assertEquals(1, buildSpec(listOf(SCSpecEntryXdr.EventV0(SCSpecEventV0Xdr("","",SCSymbolXdr("Ev"),emptyList(),emptyList(),SCSpecEventDataFormatXdr.SC_SPEC_EVENT_DATA_FORMAT_SINGLE_VALUE)))).events().size) }

    @Test fun testFindEntry_func() { assertNotNull(buildSpec(listOf(funcEntry("f"))).findEntry("f")) }
    @Test fun testFindEntry_struct() { assertNotNull(buildSpec(listOf(SCSpecEntryXdr.UdtStructV0(SCSpecUDTStructV0Xdr("","","S",emptyList())))).findEntry("S")) }
    @Test fun testFindEntry_union() { assertNotNull(buildSpec(listOf(SCSpecEntryXdr.UdtUnionV0(SCSpecUDTUnionV0Xdr("","","U",emptyList())))).findEntry("U")) }
    @Test fun testFindEntry_enum() { assertNotNull(buildSpec(listOf(SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","E",emptyList())))).findEntry("E")) }
    @Test fun testFindEntry_errEnum() { assertNotNull(buildSpec(listOf(SCSpecEntryXdr.UdtErrorEnumV0(SCSpecUDTErrorEnumV0Xdr("","","EE",emptyList())))).findEntry("EE")) }
    @Test fun testFindEntry_event() { assertNotNull(buildSpec(listOf(SCSpecEntryXdr.EventV0(SCSpecEventV0Xdr("","",SCSymbolXdr("Ev"),emptyList(),emptyList(),SCSpecEventDataFormatXdr.SC_SPEC_EVENT_DATA_FORMAT_SINGLE_VALUE)))).findEntry("Ev")) }
    @Test fun testFindEntry_notFound() { assertNull(buildSpec(emptyList()).findEntry("x")) }

    @Test fun testEnumByName() {
        val e = SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","C", listOf(SCSpecUDTEnumCaseV0Xdr("","Red",Uint32Xdr(0u)), SCSpecUDTEnumCaseV0Xdr("","Green",Uint32Xdr(1u)))))
        assertEquals(1u, (buildSpec(listOf(e)).nativeToXdrSCVal("Green", SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("C"))) as SCValXdr.U32).value.value)
    }
    @Test fun testEnumByNameNotFound() {
        val e = SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","C", listOf(SCSpecUDTEnumCaseV0Xdr("","Red",Uint32Xdr(0u)))))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(e)).nativeToXdrSCVal("Blue", SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("C"))) }
    }
    @Test fun testEnumInvalidVal() {
        val e = SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","C", listOf(SCSpecUDTEnumCaseV0Xdr("","Red",Uint32Xdr(0u)))))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(e)).nativeToXdrSCVal(99, SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("C"))) }
    }
    @Test fun testEnumBadType() {
        val e = SCSpecEntryXdr.UdtEnumV0(SCSpecUDTEnumV0Xdr("","","C", emptyList()))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(e)).nativeToXdrSCVal(listOf(1), SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("C"))) }
    }

    @Test fun testUnionVoid() {
        val u = SCSpecEntryXdr.UdtUnionV0(SCSpecUDTUnionV0Xdr("","","U", listOf(SCSpecUDTUnionCaseV0Xdr.VoidCase(SCSpecUDTUnionCaseVoidV0Xdr("","None")))))
        assertTrue(buildSpec(listOf(u)).nativeToXdrSCVal(NativeUnionVal.VoidCase("None"), SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("U"))) is SCValXdr.Vec)
    }
    @Test fun testUnionBadCase() {
        val u = SCSpecEntryXdr.UdtUnionV0(SCSpecUDTUnionV0Xdr("","","U", listOf(SCSpecUDTUnionCaseV0Xdr.VoidCase(SCSpecUDTUnionCaseVoidV0Xdr("","None")))))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(u)).nativeToXdrSCVal(NativeUnionVal.VoidCase("X"), SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("U"))) }
    }
    @Test fun testUnionBadType() {
        val u = SCSpecEntryXdr.UdtUnionV0(SCSpecUDTUnionV0Xdr("","","U", emptyList()))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(u)).nativeToXdrSCVal("bad", SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("U"))) }
    }

    @Test fun testStructBadType() {
        val s = SCSpecEntryXdr.UdtStructV0(SCSpecUDTStructV0Xdr("","","S", listOf(SCSpecUDTStructFieldV0Xdr("","f",u32Type))))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(s)).nativeToXdrSCVal("bad", SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("S"))) }
    }
    @Test fun testStructMissingField() {
        val s = SCSpecEntryXdr.UdtStructV0(SCSpecUDTStructV0Xdr("","","S", listOf(SCSpecUDTStructFieldV0Xdr("","f",u32Type))))
        assertFailsWith<ContractSpecException> { buildSpec(listOf(s)).nativeToXdrSCVal(mapOf("x" to 1), SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("S"))) }
    }
    @Test fun testUdtNotFound() { assertFailsWith<ContractSpecException> { buildSpec(emptyList()).nativeToXdrSCVal(42, SCSpecTypeDefXdr.Udt(SCSpecTypeUDTXdr("Missing"))) } }

    @Test fun testCSE_toString() { assertTrue(ContractSpecException.functionNotFound("f").toString().contains("f")) }
    @Test fun testCSE_argNotFound() { val e = ContractSpecException.argumentNotFound("a", "f"); assertTrue(e.toString().contains("a") && e.toString().contains("f")) }
    @Test fun testCSE_entryNotFound() { assertEquals("T", ContractSpecException.entryNotFound("T").entryName) }
    @Test fun testCSE_factories() { assertTrue(ContractSpecException.invalidType("x").message!!.contains("Invalid")); assertTrue(ContractSpecException.conversionFailed("x").message!!.contains("Conversion")); assertTrue(ContractSpecException.invalidEnumValue("x").message!!.contains("enum")) }
}
