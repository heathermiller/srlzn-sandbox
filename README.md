    [info] Running JavaSerializationListBench 5
    JavaSerializationListBench$ 303 139 110 124 100
    Bytes: 1000297

    (Kryo 2.x)
    [info] Running KryoListBench 5
    KryoListBench$  157 41  11  6   6
    Bytes: 524288 (52% the size of Java serialized representation, though in Kryo 2 this seems to grow per repitition)

    NEW: (Kryo 2.x)
    [info] Running KryoVectorBench 5
    KryoVectorBench$    93  41  6   6   6
    Bytes: 524288 (52% the size of Java serialized representation, though in Kryo 2 this seems to grow per repitition)

    OLD: (Kryo 1.x)
    [info] Running KryoVectorBench 5
    KryoVectorBench$  192 139 57  49  44
    Bytes: 514098 (51% the size of Java serialized representation)

    [info] Running PicklerListBench 5
    PicklerListBench$ 43  5 6 7 9
    Bytes: 400004 (40% the size of Java serialized representation)

    [info] Running PicklerUnsafeListBench 5
    PicklerUnsafeListBench$	23	14	3	3	3
    Bytes: 400004 (40% the size of Java serialized representation)