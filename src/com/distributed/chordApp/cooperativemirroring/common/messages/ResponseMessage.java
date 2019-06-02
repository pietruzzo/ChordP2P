package com.distributed.chordApp.cooperativemirroring.common.messages;

import com.distributed.chordApp.cooperativemirroring.common.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Class that represent the response to a request message
 */
public class ResponseMessage implements Serializable {
    private static final long serialVersionUID = 50002L;
    //IP address of the host that reply to the request
    private String solverHostIP = null;
    //Port of the solver host
    private Integer solverHostPort = null;
    //Reference to the original request
    private RequestMessage originalRequest = null;
    //Boolean flag used for decree if a certain request has been successfully done
    private Boolean requestPerformedSuccessfully = null;
    //Result of a retrieve request
    private Resource requestedResource = null;

    public ResponseMessage(String solverHostIP, Integer solverHostPort, RequestMessage originalRequest, Boolean requestPerformedSuccessfully, @Nullable Resource requestedResource){
        this.setSolverHostIP(solverHostIP);
        this.setSolverHostPort(solverHostPort);
        this.setOriginalRequest(originalRequest);
        this.setRequestPerformedSuccessfully(requestPerformedSuccessfully);
        this.setRequestedResource(requestedResource);
    }

    /*Setter methods*/
    private void setSolverHostIP(String solverHostIP){this.solverHostIP = solverHostIP; }
    private void setSolverHostPort(Integer solverHostPort){this.solverHostPort = solverHostPort; }
    private void setOriginalRequest(RequestMessage originalRequest){this.originalRequest = originalRequest; }
    private void setRequestPerformedSuccessfully(Boolean requestPerformedSuccessfully){this.requestPerformedSuccessfully = requestPerformedSuccessfully; }
    private void setRequestedResource(Resource requestedResource){this.requestedResource = requestedResource; }

    /*Getter methods*/
    public String getSolverHostIP(){ return this.solverHostIP; }
    public Integer getSolverHostPort(){ return this.solverHostPort; }
    public RequestMessage getOriginalRequest(){ return this.originalRequest; }
    public Boolean getRequestPerformedSuccessfully(){ return this.requestPerformedSuccessfully; }
    public Resource getRequestedResource(){ return this.requestedResource; }

    /**
     * Method used for writing a concise representation of the response
     * @return
     */
    public String conciseToString(){
        String state = "RESPONSE\n{" ;

        state += "[sender=@" + this.originalRequest.getOriginalSenderIP() + ":" + this.originalRequest.getOriginalSenderPort() + "] ";
        state += "[operation=";
        if(this.getOriginalRequest().getDepositResource()){
            state +="deposit] ";
        }else{
            state += "retrieve] ";
        }
        state +="[resource ID=" + this.originalRequest.getResourceID() + "] ";
        state += "[solver=@" + this.getSolverHostIP() + ":" + this.getSolverHostPort() + "] ";
        state += "[outcome=";
        if(this.getRequestPerformedSuccessfully()){
            state += "success]}";
        }else{
            state += "failure]}";
        }

        return state;
    }

    @Override
    public String toString(){
        String state = "\n======{RESPONSE MESSAGE}======\n";

        state += "\nOriginal sender IP: " + this.getOriginalRequest().getOriginalSenderIP();
        state += "\nOriginal sender port: " + this.getOriginalRequest().getOriginalSenderPort();
        state += "\nResource: " + this.getOriginalRequest().getResourceID();
        if(this.getOriginalRequest().getDepositResource()) state +="\n<Deposit request>";
        else state += "\n<Retrieve request>";
        state += "\nSolver host IP: " + this.getSolverHostIP();
        state += "\nSolver host port: " + this.getSolverHostPort();
        state += "\nOutcome : ";
        if(this.getRequestPerformedSuccessfully()) state += "SUCCESS";
        else state += "FAILURE";

        state += "\n===========================\n";

        return state;
    }
}
