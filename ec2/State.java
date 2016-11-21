package ec2;

import com.amazonaws.services.ec2.model.Instance;

public enum State {
	Pending("pending"),
	Running("running"),
	Stopping("stopping"),
	Stopped("stopped"),
	Terminated("terminated"),
	ShuttingDown("shutting-down"),
	UnknownState("unknown state"),
	;
	String text = "";
	State(String text)
	{
		this.text = text;
	}
	public static State GetState(Instance inst)
	{
		return GetState(inst.getState().getName());
	}
	public static State GetState(String fromString)
	{
		for (int i = 0; i < State.values().length; ++i)
		{
			State s = State.values()[i];
			if (s.text.equals(fromString))
				return s;
		}
		return UnknownState;
	}
	

}
