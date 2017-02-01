package com.hpe.application.automation.tfs;

import com.hpe.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hpe.application.automation.tools.common.model.AutEnvironmentParameterModel;
import com.hpe.application.automation.tools.common.model.AutEnvironmentParameterType;
import com.hpe.application.automation.tools.common.rest.RestClient;
import com.hpe.application.automation.tools.common.sdk.AUTEnvironmentBuilderPerformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ybobrik on 1/22/2017.
 */

enum EnvConfigAction {
    newconf,
    existing
}

enum EnvParamType {
    json,
    environment,
    manual
}

public class LabEnvPrepTask extends AbstractTask {
    private String AlmServ;
    private String UserName;
    private String Pass;
    private String Domain;
    private String Project;
    private int EnvId;
    private EnvConfigAction Action;
    private String NewConfName;
    private String AssignAutConfTo;
    private int UseAsConfId;
    private String PathToJSON;
    private boolean ParamOnlyFirst;
    private List<AutEnvironmentParameterModel> AutEnvironmentParameters = new ArrayList<AutEnvironmentParameterModel>();
//    private EnvParamType ParamType;
//    private String ParamName;
//    private String ParamValue;

    public void parseArgs(String[] args) throws Exception {
        if (args.length < 1) {
            throw new Exception("'ALM server' parameter missing");
        }
        this.AlmServ = args[0];

        if (args.length < 2) {
            throw new Exception("'User name' parameter missing");
        }
        this.UserName = args[1];

        if (args.length < 3) {
            throw new Exception("'Password' parameter missing");
        }
        try {
            this.Pass = extractPasswordFromParameter(args[2]);
        } catch (Throwable th) {
            throw new Exception("Failed to extract 'Password' from argument (use 'pass:' prefix)");
        }

        if (args.length < 4) {
            throw new Exception("'Domain' parameter missing");
        }
        this.Domain = args[3];

        if (args.length < 5) {
            throw new Exception("'Project' parameter missing");
        }
        this.Project = args[4];

        if (args.length < 6) {
            throw new Exception("'AUT Environment ID' parameter missing");
        }
        try {
            this.EnvId = Integer.valueOf(args[5]);
        }
        catch (Throwable th) {
            throw new Exception("Failed to parse AUT Environment ID parameter (use unsigned integer value)");
        }

        if (args.length < 7) {
            throw new Exception("'New/Existing' parameter missing");
        }
        try {
            this.Action = Enum.valueOf(EnvConfigAction.class, args[6].toLowerCase());
        }
        catch (Throwable th) {
            throw new Exception("Failed to parse 'New/Existing' parameter (use 'newconf' or 'existing' value)");
        }

        if (args.length < 8) {
            throw new Exception("'Create new configuration named' parameter missing");
        }
        try {
            this.NewConfName = extractvalueFromParameter(args[7], "newnamed:");
        }
        catch (Throwable th) {
            throw new Exception("Failed to extract 'Create new configuration named' parameter (use 'newnamed:' prefix)");
        }

        if (args.length < 9) {
            throw new Exception("'Assign AUT Environment...' parameter missing");
        }
        try {
            this.AssignAutConfTo = extractvalueFromParameter(args[8], "assign:");
        }
        catch (Throwable th) {
            throw new Exception("Failed to extract 'Assign AUT Environment...' parameter (use 'assign:' prefix)");
        }

        if (this.Action == EnvConfigAction.existing) {
            if (args.length < 10) {
                throw new Exception("'Use as existing config with ID' parameter missing");
            }
            try {
                this.UseAsConfId = Integer.valueOf(extractvalueFromParameter(args[9], "useasexisting:").toLowerCase());
            } catch (Throwable th) {
                throw new Exception("Failed to parse 'Use as existing config with ID' parameter (use unsigned integer value with 'useasexisting:' prefix)");
            }
        }

        if (args.length < 11) {
            throw new Exception("'Path to JSON file' parameter missing");
        }
        try {
            this.PathToJSON = extractvalueFromParameter(args[10], "jsonpath:");
        }
        catch (Throwable th) {
            throw new Exception("Failed to extract 'Path to JSON file' parameter (use 'jsonpath:' preffix)");
        }

        if (args.length < 12) {
            throw new Exception("'Get only the first value...' parameter missing");
        }
        try {
            this.ParamOnlyFirst =  Boolean.parseBoolean(args[11]);
        }
        catch (Throwable th) {
            throw new Exception("Failed to extract 'Get only the first value...' parameter");
        }

        int i = 1;
        while (i <= (args.length - 12)/3) {
            String paramName = extractvalueFromParameter(args[12 + (i-1)*3 + 1], "parname" + Integer.toString(i) + ":");
            String paramValue = extractvalueFromParameter(args[12 + (i-1)*3 + 2], "parval" + Integer.toString(i) + ":");
            EnvParamType paramType;
            try {
                paramType = Enum.valueOf(EnvParamType.class, extractvalueFromParameter(args[12 + (i-1)*3], "partype" + Integer.toString(i) + ":").toLowerCase());
            }
            catch (Throwable th) {
                throw new Exception("Failed to parse 'Parameter type' of " + Integer.toString(i) + " parameter value: '" + args[12 + (i-1)*3] + "'. Use 'json' or 'environment' or 'manual' value.");
            }
            AutEnvironmentParameterType type = convertType(paramType);

            AutEnvironmentParameters.add(new AutEnvironmentParameterModel(paramName, paramValue, type, this.ParamOnlyFirst));
            i++;
        }
//        if (args.length < 13) {
//            throw new Exception("'Parameter type' parameter missing");
//        }
//        try {
//            this.ParamType = Enum.valueOf(EnvParamType.class, args[12].toLowerCase());
//        }
//        catch (Throwable th) {
//            throw new Exception("Failed to parse 'Parameter type' parameter (use 'fromjson' or 'environment' or 'manual' value)");
//        }
//
//        if (args.length < 14) {
//            throw new Exception("'Parameter name' parameter missing");
//        }
//        this.ParamName = args[13];
//
//        if (args.length < 15) {
//            throw new Exception("'Parameter value' parameter missing");
//        }
//        this.ParamValue = args[14];
    }

    public void execute() throws Throwable {
        boolean useExistingAutEnvConf = Action == Action.existing;

//        for(AlmConfigParameter prm: AlmLabEnvPrepareTaskConfigurator.fetchAlmParametersFromContext(confMap))
//        {
//        }

        RestClient restClient = new RestClient(AlmServ, Domain, Project, UserName);

        AutEnvironmentConfigModel autEnvModel = new AutEnvironmentConfigModel(
                AlmServ,
                UserName,
                Pass,
                Domain,
                Project,
                useExistingAutEnvConf,
                String.valueOf(EnvId),
                useExistingAutEnvConf ? String.valueOf(UseAsConfId) : NewConfName,
                PathToJSON,
                AutEnvironmentParameters);

            AUTEnvironmentBuilderPerformer performer = new AUTEnvironmentBuilderPerformer(restClient, new RestLogger(), autEnvModel);
            performer.start();

//            String outputConfig = AssignAutConfTo;
//
//            if (!StringUtils.isNullOrEmpty(outputConfig)) {
//
//                String confId = autEnvModel.getCurrentConfigID();
//                variableService.saveGlobalVariable(outputConfig, confId);
//            }
    }

    private AutEnvironmentParameterType convertType(EnvParamType sourceType) {
        switch (sourceType) {
            case environment: return AutEnvironmentParameterType.ENVIRONMENT;
            case manual: return AutEnvironmentParameterType.USER_DEFINED;
            case json: return AutEnvironmentParameterType.EXTERNAL;
            default: return AutEnvironmentParameterType.UNDEFINED;
        }
    }
}
