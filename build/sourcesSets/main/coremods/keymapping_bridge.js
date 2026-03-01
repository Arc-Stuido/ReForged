/*
 * Forge JS coremod: patches net.minecraft.client.KeyMapping
 * to add a bridge constructor accepting NeoForge-typed parameters.
 *
 * NeoForge mods (e.g. Jade) call:
 *   new KeyMapping(String, neo.IKeyConflictContext, neo.KeyModifier, InputConstants.Key, String)
 *
 * Forge's KeyMapping only has:
 *   KeyMapping(String, forge.IKeyConflictContext, forge.KeyModifier, InputConstants.Key, String)
 *
 * This coremod injects a new constructor that converts NeoForge types to Forge types
 * and delegates to the existing Forge constructor.
 *
 * - NeoForge IKeyConflictContext extends Forge IKeyConflictContext, so it's directly assignable.
 * - NeoForge KeyModifier is converted via name() → Forge KeyModifier.valueOf().
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    return {
        'keymapping_bridge': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.KeyMapping'
            },
            'transformer': function(classNode) {
                addNeoForgeConstructor(classNode);
                ASMAPI.log('INFO', '[ReForged] Added NeoForge-typed constructor bridge to KeyMapping');
                return classNode;
            }
        }
    };
}

/**
 * Injects:
 * public KeyMapping(String description, neo.IKeyConflictContext ctx, neo.KeyModifier mod, InputConstants.Key key, String category) {
 *     this(description, (forge.IKeyConflictContext) ctx, forge.KeyModifier.valueOf(mod.name()), key, category);
 * }
 */
function addNeoForgeConstructor(classNode) {
    var neoDesc = '('
        + 'Ljava/lang/String;'
        + 'Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;'
        + 'Lnet/neoforged/neoforge/client/settings/KeyModifier;'
        + 'Lcom/mojang/blaze3d/platform/InputConstants$Key;'
        + 'Ljava/lang/String;'
        + ')V';

    var forgeDesc = '('
        + 'Ljava/lang/String;'
        + 'Lnet/minecraftforge/client/settings/IKeyConflictContext;'
        + 'Lnet/minecraftforge/client/settings/KeyModifier;'
        + 'Lcom/mojang/blaze3d/platform/InputConstants$Key;'
        + 'Ljava/lang/String;'
        + ')V';

    var method = new MethodNode(
        Opcodes.ACC_PUBLIC,
        '<init>',
        neoDesc,
        null, null
    );

    // this
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    // description (String)
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    // keyConflictContext: NeoForge IKeyConflictContext extends Forge IKeyConflictContext, directly assignable
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    // Convert NeoForge KeyModifier -> Forge KeyModifier: mod.name() -> ForgeKeyModifier.valueOf(name)
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
    method.instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'net/neoforged/neoforge/client/settings/KeyModifier',
        'name',
        '()Ljava/lang/String;',
        false
    ));
    method.instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        'net/minecraftforge/client/settings/KeyModifier',
        'valueOf',
        '(Ljava/lang/String;)Lnet/minecraftforge/client/settings/KeyModifier;',
        false
    ));
    // keyCode (InputConstants.Key)
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
    // category (String)
    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 5));

    // Delegate to existing Forge 5-arg constructor: this(String, forge.IKeyConflictContext, forge.KeyModifier, Key, String)
    method.instructions.add(new MethodInsnNode(
        Opcodes.INVOKESPECIAL,
        'net/minecraft/client/KeyMapping',
        '<init>',
        forgeDesc,
        false
    ));

    method.instructions.add(new InsnNode(Opcodes.RETURN));

    method.maxStack = 6;
    method.maxLocals = 6;
    classNode.methods.add(method);
}
