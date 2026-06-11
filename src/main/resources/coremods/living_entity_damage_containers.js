/**
 * LivingEntity.damageContainers field patch.
 *
 * NeoForge's damage pipeline adds:
 *   protected final Stack<DamageContainer> damageContainers
 * to LivingEntity. NeoForge mod mixins commonly @Shadow this field
 * (e.g. SuperbWarfare's LivingEntityMixin); @Shadow can only resolve
 * members that are native to the target class, so the field must be
 * injected by a coremod before Mixin processes the class.
 *
 * Initialized to an empty java.util.Stack at the end of each constructor.
 *
 * Target: net.minecraft.world.entity.LivingEntity
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');

function initializeCoreMod() {
    return {
        'living_entity_damage_containers': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.LivingEntity'
            },
            'transformer': function(classNode) {
                var fields = classNode.fields;
                for (var i = 0; i < fields.size(); i++) {
                    if (fields.get(i).name === 'damageContainers') {
                        ASMAPI.log('INFO', '[ReForged] LivingEntity.damageContainers already exists, skipping');
                        return classNode;
                    }
                }

                // protected Stack damageContainers;  (signature carries the generic type)
                classNode.fields.add(new FieldNode(
                    Opcodes.ACC_PROTECTED,
                    'damageContainers',
                    'Ljava/util/Stack;',
                    'Ljava/util/Stack<Lnet/neoforged/neoforge/common/damagesource/DamageContainer;>;',
                    null
                ));

                // this.damageContainers = new Stack();  before every constructor RETURN
                var methods = classNode.methods;
                var patched = 0;
                for (var m = 0; m < methods.size(); m++) {
                    var method = methods.get(m);
                    if (method.name !== '<init>') continue;

                    var returns = [];
                    var instructions = method.instructions;
                    for (var i = 0; i < instructions.size(); i++) {
                        var insn = instructions.get(i);
                        if (insn.getOpcode() === Opcodes.RETURN) {
                            returns.push(insn);
                        }
                    }
                    for (var r = 0; r < returns.length; r++) {
                        var init = new InsnList();
                        init.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        init.add(new TypeInsnNode(Opcodes.NEW, 'java/util/Stack'));
                        init.add(new InsnNode(Opcodes.DUP));
                        init.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, 'java/util/Stack', '<init>', '()V', false));
                        init.add(new FieldInsnNode(Opcodes.PUTFIELD,
                            'net/minecraft/world/entity/LivingEntity',
                            'damageContainers',
                            'Ljava/util/Stack;'));
                        instructions.insertBefore(returns[r], init);
                        patched++;
                    }
                }

                ASMAPI.log('INFO', '[ReForged] Added LivingEntity.damageContainers field (' + patched + ' ctor return site(s) patched)');
                return classNode;
            }
        }
    };
}
