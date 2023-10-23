package hai913i.tp2.utils;

public class Pair<L,R> {

	L left;
	R right;
	
	
	public Pair() {
		super();
	}

	public Pair(L left, R right) {
		super();
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public void setRight(R right) {
		this.right = right;
	}
	
	
	
}