package cheeseum.lazyzombies;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cheeseum.obfhelper.ObfuscationMapper;
import cheeseum.obfhelper.ObfuscationMapper.MethodData;
import cpw.mods.fml.common.FMLLog;

public class LazyZombiesTransformerASM implements IClassTransformer, Opcodes
{
	// XXX: this is initialized out in the loading plugin and it feels hacky
	public static ObfuscationMapper obfMapper;
	
	private String fieldDesc (String c)
	{
		return "L" + c + ";";
	}
	
	private boolean methodEquals (MethodNode mn, MethodData mData)
	{
		return mn.name.equals(mData.name) && mn.desc.equals(mData.desc); 
	}

	private boolean methodEquals (MethodInsnNode mn, MethodData mData)
	{
		return mn.name.equals(mData.name) && mn.desc.equals(mData.desc); 
	}
	
	private byte[] transformEntityZombie(String className, byte[] in)
	{
		ClassNode cNode = new ClassNode();
		ClassReader cr = new ClassReader(in);
		cr.accept(cNode, 0);
		
		// class, field, and method mappings from mcp
		String c_World = obfMapper.getClassMapping("net/minecraft/world/World", "World");
		MethodData m_addTask = obfMapper.getMethodMapping("addTask", "func_75776_a", "(ILnet/minecraft/entity/ai/EntityAIBase;)V");
		
		for (MethodNode mn : cNode.methods)
		{
			if (mn.name.equals("<init>"))
			{
				FMLLog.finest("patching zombie constructor: " + mn.name + mn.desc);
				
				boolean found = false;
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();

					// add insns as we go
					newInsns.add(insn);
					
					// first task added (swimming ai)
					if (!found && insn instanceof MethodInsnNode && methodEquals((MethodInsnNode)insn, m_addTask))
					{
						FMLLog.finest("removing door breaking ai call");
						
						// skip past the next task (door breaking ai)
						while (it.hasNext())
						{
							insn = it.next();
							if (insn instanceof MethodInsnNode && methodEquals((MethodInsnNode)insn, m_addTask))
							{
								found = true;
								break;
							}
						}
					}
				}
				mn.instructions = newInsns;
			}
		}
		
		ClassWriter cw = new ClassWriter(0);
		cNode.accept(cw);
		return cw.toByteArray();
	}
	
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2)
	{
		//String className = remapper.map(arg0).replace('/','.');
		String className = arg1;
		if (className.equals("net.minecraft.entity.monster.EntityZombie")) 
		{
			FMLLog.info("Patching class %s!", className);
			return transformEntityZombie(className, arg2);
		}
		
		return arg2;
	}
}
