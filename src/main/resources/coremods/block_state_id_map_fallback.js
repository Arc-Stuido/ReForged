var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    return {
        'reforged_block_state_id_map_fallback': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.registries.GameData$BlockCallbacks'
            },
            'transformer': function(classNode) {
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    if (method.name === 'getBlockStateIDMap' && method.desc === '()Lnet/minecraft/core/IdMapper;') {
                        var body = new InsnList();
                        body.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'org/xiyu/reforged/bridge/BlockStateIdMapBridge',
                            'fallbackBlockStateIdMap',
                            '()Lnet/minecraft/core/IdMapper;',
                            false
                        ));
                        body.add(new InsnNode(Opcodes.ARETURN));

                        method.instructions.clear();
                        method.instructions.add(body);
                        method.tryCatchBlocks.clear();
                        method.maxStack = 1;
                        method.maxLocals = 0;
                        ASMAPI.log('INFO', '[ReForged] Replaced GameData.BlockCallbacks.getBlockStateIDMap() with fallback mapper');
                        return classNode;
                    }
                }

                ASMAPI.log('WARN', '[ReForged] Could not find GameData.BlockCallbacks.getBlockStateIDMap() to patch');
                return classNode;
            }
        },
        'reforged_block_clinit_state_id_map_fallback': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.Block'
            },
            'transformer': function(classNode) {
                var patched = 0;
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    if (method.name !== '<clinit>') {
                        continue;
                    }

                    var insns = method.instructions;
                    for (var node = insns.getFirst(); node !== null; node = node.getNext()) {
                        if (node.getOpcode && node.getOpcode() === Opcodes.INVOKESTATIC
                                && node.name === 'getBlockStateIDMap'
                                && node.desc === '()Lnet/minecraft/core/IdMapper;'
                                && (node.owner === 'net/minecraftforge/registries/GameData$BlockCallbacks'
                                    || node.owner === 'net/minecraftforge/registries/GameData'
                                    || node.owner === 'net/neoforged/neoforge/registries/GameData')) {
                            node.owner = 'org/xiyu/reforged/bridge/BlockStateIdMapBridge';
                            node.name = 'fallbackBlockStateIdMap';
                            node.itf = false;
                            patched++;
                        }
                    }
                }

                if (patched > 0) {
                    ASMAPI.log('INFO', '[ReForged] Redirected Block.<clinit> block-state ID map lookup to fallback mapper (' + patched + ' call(s))');
                } else {
                    ASMAPI.log('WARN', '[ReForged] Could not find Block.<clinit> block-state ID map lookup to patch');
                }
                return classNode;
            }
        },
        'reforged_entity_data_serializers_state_id_map_fallback': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.network.syncher.EntityDataSerializers'
            },
            'transformer': function(classNode) {
                var patched = 0;
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    if (method.name !== '<clinit>') {
                        continue;
                    }

                    var insns = method.instructions;
                    for (var node = insns.getFirst(); node !== null; ) {
                        var next = node.getNext();
                        if (node.getOpcode && node.getOpcode() === Opcodes.GETSTATIC
                                && node.owner === 'net/minecraft/world/level/block/Block'
                                && node.name === 'BLOCK_STATE_REGISTRY'
                                && node.desc === 'Lnet/minecraft/core/IdMapper;') {
                            insns.insertBefore(node, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                'org/xiyu/reforged/bridge/BlockStateIdMapBridge',
                                'fallbackBlockStateIdMap',
                                '()Lnet/minecraft/core/IdMapper;',
                                false
                            ));
                            insns.remove(node);
                            patched++;
                        }
                        node = next;
                    }
                }

                if (patched > 0) {
                    ASMAPI.log('INFO', '[ReForged] Redirected EntityDataSerializers block-state ID map lookup to fallback mapper (' + patched + ' field(s))');
                } else {
                    ASMAPI.log('WARN', '[ReForged] Could not find EntityDataSerializers block-state ID map lookup to patch');
                }
                return classNode;
            }
        },
        'reforged_synched_entity_data_disable_caller_class_lookup': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.network.syncher.SynchedEntityData'
            },
            'transformer': function(classNode) {
                var patched = 0;
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    if (method.name !== 'defineId') {
                        continue;
                    }

                    var insns = method.instructions;
                    var anchor = null;
                    for (var node = insns.getFirst(); node !== null; node = node.getNext()) {
                        if (node.getOpcode && node.getOpcode() === Opcodes.GETSTATIC
                                && node.owner === 'net/minecraft/network/syncher/SynchedEntityData'
                                && node.name === 'ID_REGISTRY'
                                && node.desc === 'Lnet/minecraft/util/ClassTreeIdRegistry;') {
                            anchor = node;
                            break;
                        }
                    }

                    if (anchor !== null) {
                        for (var remove = insns.getFirst(); remove !== null && remove !== anchor; ) {
                            var next = remove.getNext();
                            insns.remove(remove);
                            remove = next;
                            patched++;
                        }
                        method.tryCatchBlocks.clear();
                    }
                }

                if (patched > 0) {
                    ASMAPI.log('INFO', '[ReForged] Removed SynchedEntityData.defineId caller Class.forName prelude (' + patched + ' instruction(s))');
                } else {
                    ASMAPI.log('WARN', '[ReForged] Could not find SynchedEntityData.defineId caller Class.forName prelude to patch');
                }
                return classNode;
            }
        }
    };
}
