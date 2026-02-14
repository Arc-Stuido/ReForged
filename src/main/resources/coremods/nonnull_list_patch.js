/*
 * Forge JS coremod: patches net.minecraft.core.NonNullList to add NeoForge's
 * codecOf(Codec) and copyOf(Collection) static methods.
 *
 * NeoForge adds these via source patches; we replicate them at the bytecode
 * level so NeoForge mods (like Create) that call NonNullList.codecOf() work.
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

function initializeCoreMod() {
    return {
        'nonnulllist_codecof': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.core.NonNullList'
            },
            'transformer': function(classNode) {
                // Add: public static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec)
                // Implementation: entryCodec.listOf().xmap(list -> { NNL r = create(); r.addAll(list); return r; }, Function.identity())
                //
                // Since creating lambdas in ASM is extremely complex, we use a simpler approach:
                // We add a helper that calls a utility class we control.
                addCodecOfMethod(classNode);
                addCopyOfMethod(classNode);
                return classNode;
            }
        }
    };
}

function addCodecOfMethod(classNode) {
    // public static Codec codecOf(Codec entryCodec)
    // Due to type erasure, the raw signature is: (Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;
    //
    // Implementation strategy: delegate to org.xiyu.reforged.util.NonNullListHelper.codecOf(Codec)
    var methodNode = new MethodNode(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        'codecOf',
        '(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;',
        null, // signature (generic) - not needed for runtime
        null  // exceptions
    );
    methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load entryCodec
    methodNode.instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        'org/xiyu/reforged/util/NonNullListHelper',
        'codecOf',
        '(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;',
        false
    ));
    methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
    methodNode.maxStack = 1;
    methodNode.maxLocals = 1;
    classNode.methods.add(methodNode);
}

function addCopyOfMethod(classNode) {
    // public static NonNullList copyOf(Collection entries)
    // Delegates to org.xiyu.reforged.util.NonNullListHelper.copyOf(Collection)
    var methodNode = new MethodNode(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        'copyOf',
        '(Ljava/util/Collection;)Lnet/minecraft/core/NonNullList;',
        null,
        null
    );
    methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load entries
    methodNode.instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        'org/xiyu/reforged/util/NonNullListHelper',
        'copyOf',
        '(Ljava/util/Collection;)Lnet/minecraft/core/NonNullList;',
        false
    ));
    methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
    methodNode.maxStack = 1;
    methodNode.maxLocals = 1;
    classNode.methods.add(methodNode);
}
