package hussachai.osu.os2.system.storage;

public class MemoryManager {

	private Memory memory;
	
	public MemoryManager(Memory memory){
		this.memory = memory;
	}
	
	public Memory getMemory(){
		return memory;
	}
}
