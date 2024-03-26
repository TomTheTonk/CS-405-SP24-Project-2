import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContigousMemoryAllocator {
	private int size;    // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap;   // map process to partition
	private List<Partition> partList;    // list of memory partitions
	// constructor
	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size)); //add the first hole, which is the whole memory at start up
	}
      
	// prints the list of available commands
	public void print_help_message() {
		//TODO: add code below
		System.out.println("RQ <process> <size> to request memory of size for the process");
		System.out.println("RL <process> to release the memory of the process");
		System.out.println("STAT to show the memory allocation status");
		System.out.println("EXIT to exit");
		System.out.println("HELP to show the availiable comands");
	}
      
	// prints the allocation map (free + allocated) in ascending order of base addresses
	public void print_status() {
		//TODO: add code below
		order_partitions();
		System.out.printf("Partitions [Allocated = %d B, Free = %d B]:\n", allocated_memory(), free_memory());
		for(Partition part: partList) {
			System.out.printf("Address [%d:%d] status (%s B)\n",
					part.getBase(), part.getBase() + part.getLength()-1, part.isFreeOrNot()?"Free": part.getProcess());
		}
	}
      
	// get the size of total allocated memory
	private int allocated_memory() {
		//TODO: add code below
		int allocated = 0;
		for(Partition part: partList) {
			if(!part.isFreeOrNot()) {
				allocated += part.getLength();
				
			}
		}
		return allocated;
	}
      
	// get the size of total free memory
	private int free_memory() {
		//TODO: add code below
		int free = 0;
		for(Partition part: partList) {
			if(part.isFreeOrNot()) {
				free += part.getLength();
				
			}
		}
		return free;
		
	}
      
	// sort the list of partitions in ascending order of base addresses
	private void order_partitions() {
		//TODO: add code below
		Collections.sort(partList, Comparator.comparingInt(Partition::getBase));
	}
      
	// implements the first fit memory allocation algorithm
	public int first_fit(String process, int size) {
		//TODO: add code below
		if(allocMap.containsKey(process) ) {
			return -1;
		}
		int index = 0;
		int alloc = -1;
		
		while(index < partList.size()) {
			Partition part = partList.get(index);
			if(part.isFreeOrNot() && part.getLength() >= size) {
				Partition newPart = new Partition(part.getBase(), size);
				newPart.setFreeOrNot(false);
				newPart.setProcess(process);
				partList.add(index, newPart);
				allocMap.put(process, newPart);
				part.setBase(part.getBase()+size);
				part.setLength(part.getLength()-size);
				if(part.getLength() == 0) {
					partList.remove(part);
					alloc = size;
					break;
				}
			}
		}
		return alloc;
	}
      
	// release the allocated memory of a process
	public int release(String process) {
		//TODO: add code below
		int free = -1;
		for(Partition part : partList) {
			if(!part.isFreeOrNot() && part.getProcess().equals(process)) {
				part.setFreeOrNot(true);
				part.setProcess(null);
				free = part.getLength();
				break;
			}
		}
		if(free < 0) {
			return free; //failed search
		}
		//Merge stuffs like the adjacent holes
		merge_holes();
		return free;
	}      
      
	// procedure to merge adjacent holes
	private void merge_holes() {
		//TODO: add code below
		
		order_partitions();
		int i = 0;
		while(i < partList.size() ) {
			Partition part = partList.get(i);
			if(!part.isFreeOrNot()) {
				continue;
			}
			
			int end_i = part.getBase() + part.getLength() - 1;
			int j = i +1;
			while(j < partList.size() && partList.get(j).isFreeOrNot()) {
				//merge j into i
				int start_j = partList.get(i).getBase();
				if(start_j == end_i + 1) {
					part.setLength(part.getLength() + partList.get(j).getLength());
					partList.remove(j);
				} else {
					break;
				} //end second while loop
				
				
			}
			
			i++;
		}//end first while loop
		
	}
} 
