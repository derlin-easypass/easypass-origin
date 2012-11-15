package inc;

import java.lang.reflect.Field;


public class DataManager implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	Node[] nodesArray;
	int arrayIndex;
	
	public DataManager(int size){
		
		if(size < 1)
			throw new IllegalArgumentException("size parameter must be positive");
		
		this.nodesArray = new Node[size];
		this.arrayIndex = 0;

	}//end constructor
	
	
	public void addNode(String name, String pseudo, String emailAdress, String pass, String notes){
		
		//if the array is full, double its size
		if(this.arrayIndex >= nodesArray.length){
			
			Node[] newArray = new Node[this.arrayIndex + 50];
			System.arraycopy(this.nodesArray, 0, newArray, 0, this.nodesArray.length);
			this.nodesArray = newArray;
						
		}
		
		this.nodesArray[this.arrayIndex] = new Node(name, pseudo, emailAdress, pass, notes);
		this.arrayIndex++;
		
	}//end addNode
	
	
	public void eraseNode(int id){
		
		if(id < 0 || id > arrayIndex){
			throw new IllegalArgumentException("this id does not exist");
		}
		
		this.nodesArray[id] = null; 
	}//end eraseNode
	
	
	public void modifyNode(int index, String attributeName, String newValue){
		
		switch(attributeName){
		
		case "pseudo" : 
		}
	}
	

	public String[] getAttributeNames(){			
		return new Node().getFieldNames();		
	}
	
	
	
	public void updateNode(int id, String columnName, String value) 
	        throws IllegalArgumentException, IllegalAccessException, 
	        NoSuchFieldException, SecurityException{
	    
	    Node.class.getField(columnName).set(this.nodesArray[id], value);
	    
	}
	
	
	public Node getNodeAt(int id) throws IllegalArgumentException{
	    if(id < 0 || id >= this.arrayIndex)
	        throw new IllegalArgumentException("this id is out of range");
	    
	    return this.nodesArray[id];
	}
	
	public boolean nodeExists(int id){
	    if(id >= this.arrayIndex || id < 0)
	        return false;
	    return true;
	}
	
	public void refactor(){
		
		Node[] newArray = new Node[this.nodesArray.length];
		int newArrayIndex = 0;
		
		for(int i = 0; i < this.arrayIndex; i++){
			if(this.nodesArray[i] != null){
				newArray[newArrayIndex++] = this.nodesArray[i];
			}
		}//end for
		
		this.nodesArray = newArray;
		this.arrayIndex = newArrayIndex;
		
	}//end refactor
	
	
	public Object[][] dataAsObjectArray() 
			throws IllegalArgumentException, IllegalAccessException{
		
		
		Field[] fields = Node.class.getDeclaredFields();
		
		Object[][] obj = new Object[this.arrayIndex + 1][fields.length + 1];
		int index = 0;
		
		
		for(int i = this.arrayIndex - 1; i >= 0; i--){
			if(this.nodesArray[i] != null){
				
				obj[index][0] = i;
				for(int fs = 0; fs < fields.length; fs++){		
					obj[index][fs + 1] = fields[fs].get(this.nodesArray[i]);
				}
				index++;
			}//end if
		}//end for
		
		obj[index][0] = this.arrayIndex;
		for(int i = 1; i < fields.length; i++){                
                obj[index][i] = "";
        }//end for
		
		return obj;
		
		
	}//end data
	

}//end class
