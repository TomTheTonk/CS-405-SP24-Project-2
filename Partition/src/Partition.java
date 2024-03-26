public class Partition {
	// the representation of each memory partition
	private int base;         // base address
	private int length;       // partition size
	private boolean freeOrNot;    // status: free or allocated
	private String process;   // assigned process if allocated

	// constructor method
	public Partition(int base, int length) {
		this.base = base;
		this.length = length;
		this.freeOrNot = true;     // free by default when creating
		this.process = null;   // unallocated to any process
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isFreeOrNot() {
		return freeOrNot;
	}

	public void setFreeOrNot(boolean freeOrNot) {
		this.freeOrNot = freeOrNot;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
		
	}
	
	@Override
	public String toString() {
		return ("Partition [base=" + base + ", Length=" + length + ", Is it free=" + freeOrNot + ", process=" + process + "]");
	}
 	
} 
