package net.LoadingChunks.SyncingFeeling.util;

public enum Slots {
	HELMET(-1),
	CHEST(-2),
	LEGGINGS(-3),
	BOOTS(-4);
	
	private Integer slot;
	
	Slots(Integer slot) {
		this.slot = slot;
	}
	
	public Integer slotNum() {
		return slot;
	}
}
