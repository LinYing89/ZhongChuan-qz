package com.example.wfsample;

import java.util.LinkedList;

public class FIFO {
	int	 bytSize=0;		//size in byte
	int  nNum=0;
	LinkedList<byte[]>	listData=new LinkedList<byte[]>();
	LinkedList<Long>	listDataTS=new LinkedList<Long>();
	
	public FIFO(){}
	public int getSize() 	 			 { return bytSize;	}
	public synchronized  int getNum()	 { return nNum;		}
	public synchronized boolean isEmpty()
	{
		return listData.isEmpty();
	}
	
	public synchronized void addLast(byte[] node, int nodeSizeInBytes)
	{
		bytSize+=nodeSizeInBytes;
		nNum++;
		byte[] bytNode=new byte[nodeSizeInBytes];
		System.arraycopy(node, 0, bytNode, 0, nodeSizeInBytes);
		listData.addLast(bytNode);
		Long objLong=Long.valueOf(System.currentTimeMillis());
		listDataTS.addLast(objLong);
	}
	
	public synchronized byte[] removeHead(long[] arrRecvTS)
	{
		if(listData.isEmpty()) return null;
		else {
			byte[] node=listData.removeFirst();
			bytSize-=node.length;
			nNum--;
			
			if(arrRecvTS!=null){
				Long objLong=listDataTS.removeFirst();
				arrRecvTS[0]=objLong.longValue();
			}
			return node; 
		}
	}
	
	public synchronized void removeAll()
	{
		if(!listData.isEmpty()) {
			listData.clear();
			listDataTS.clear();
		}
		bytSize	=0;
		nNum	=0;
	}
}
