/*
 * Forge JS coremod: patches net.minecraft.world.item.crafting.ShapedRecipePattern
 * to add NeoForge's setCraftingSize(int, int), getMaxWidth(), and getMaxHeight()
 * static methods.
 *
 * In NeoForge 1.21.1, these methods live on ShapedRecipePattern.
 * In Forge 1.21.1, the equivalent setCraftingSize/MAX_WIDTH/MAX_HEIGHT live on
 * ShapedRecipe instead. We bridge the gap by delegating to ShapedRecipe.
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');

function initializeCoreMod() {
    return {
        'shaped_recipe_pattern_patch': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.crafting.ShapedRecipePattern'
            },
            'transformer': function(classNode) {
                addSetCraftingSize(classNode);
                addGetMaxWidth(classNode);
                addGetMaxHeight(classNode);
                ASMAPI.log('INFO', '[ReForged] Added setCraftingSize/getMaxWidth/getMaxHeight to ShapedRecipePattern');
                return classNode;
            }
        }
    };
}

/**
 * public static void setCraftingSize(int width, int height) {
 *     ShapedRecipe.setCraftingSize(width, height);
 * }
 */
function addSetCraftingSize(classNode) {
    var method = new MethodNode(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        'setCraftingSize',
        '(II)V',
        null, null
    );
    method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0)); // width
    method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1)); // height
    method.instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        'net/minecraft/world/item/crafting/ShapedRecipe',
        'setCraftingSize',
        '(II)V',
        false
    ));
    method.instructions.add(new InsnNode(Opcodes.RETURN));
    method.maxStack = 2;
    method.maxLocals = 2;
    classNode.methods.add(method);
}

/**
 * public static int getMaxWidth() {
 *     return ShapedRecipe.MAX_WIDTH;
 * }
 */
function addGetMaxWidth(classNode) {
    var method = new MethodNode(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        'getMaxWidth',
        '()I',
        null, null
    );
    method.instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'net/minecraft/world/item/crafting/ShapedRecipe',
        'MAX_WIDTH',
        'I'
    ));
    method.instructions.add(new InsnNode(Opcodes.IRETURN));
    method.maxStack = 1;
    method.maxLocals = 0;
    classNode.methods.add(method);
}

/**
 * public static int getMaxHeight() {
 *     return ShapedRecipe.MAX_HEIGHT;
 * }
 */
function addGetMaxHeight(classNode) {
    var method = new MethodNode(
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        'getMaxHeight',
        '()I',
        null, null
    );
    method.instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'net/minecraft/world/item/crafting/ShapedRecipe',
        'MAX_HEIGHT',
        'I'
    ));
    method.instructions.add(new InsnNode(Opcodes.IRETURN));
    method.maxStack = 1;
    method.maxLocals = 0;
    classNode.methods.add(method);
}
