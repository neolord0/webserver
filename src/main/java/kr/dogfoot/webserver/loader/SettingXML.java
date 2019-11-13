package kr.dogfoot.webserver.loader;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SettingXML {
    public final static String Root_Node = "webserver";
    public final static String Properties_Node = "properties";
    public final static String Host_Node = "host";
    public final static String IO_Thread_Pools_Node = "io_thread_pools";
    public final static String Server_Header_Node = "server_header";
    public final static String Keep_Alive_Node = "keep_alive";
    public final static String Count_Of_Processor_Node = "count_of_processor";
    public final static String Root_Path_Node = "root_path";
    public final static String Custom_Media_Types_Node = "custom_media_types";
    public final static String SSL_Config_Node = "ssl_config";
    public final static String Proxy_Infos_Node = "proxy_infos";
    public final static String Media_Type_Node = "media_type";
    public final static String Key_Store_Node = "key_store";
    public final static String Trust_Store_Node = "trust_store";
    public final static String Path_Node = "path";
    public final static String Password_Node = "password";
    public final static String Proxy_Info_Node = "proxy_info";
    public final static String Applied_URL_Pattern_Node = "applied_url_pattern";
    public final static String Backend_Servers_Node = "backend_servers";
    public final static String Backend_Server_Node = "backend_server";
    public final static String Resource_Setting_Node = "resource_setting";
    public final static String Directory_Node = "directory";
    public final static String Virtual_Directory_Node = "virtual_directory";
    public final static String File_Node = "file";
    public final static String Filters_Node = "filters";
    public final static String Default_File_Node = "default_file";
    public final static String Not_Serviced_Files_Node = "not_serviced_files";
    public final static String Not_Serviced_File_Node = "not_serviced_file";
    public final static String Source_Path_Node = "source_path";
    public final static String Basic_Authorization_Node = "basic_authorization";
    public final static String Expect_Checking_Node = "expect_checking";
    public final static String Allowed_Method_Checking_Node = "allowed_method_checking";
    public final static String URL_Redirecting_Node = "url_redirecting";
    public final static String Header_Adding_Node = "header_adding";
    public final static String Charset_Encoding_Node = "charset_encoding";
    public final static String Content_Encoding_Node = "content_encoding";
    public final static String Fail_Condition_Node = "fail_condition";
    public final static String Fail_Condition_List_Node = "fail_condition_list";
    public final static String Adding_Condition_Node = "adding_condition";
    public final static String Adding_Condition_List_Node = "adding_condition_list";
    public final static String Header_List_Node = "header_list";
    public final static String Header_Node = "header";
    public final static String Apply_Condition_Node = "apply_condition";
    public final static String Apply_Condition_List_Node = "apply_condition_list";
    public final static String Condition_Node = "condition";
    public final static String Condition_List_Node = "condition_list";

    public final static String Send_Attr = "send";
    public final static String Timeout_Attr = "timeout";
    public final static String Max_Attr = "max";
    public final static String SSLHandshaker_Attr = "ssl_handshaker";
    public final static String RequestReceiver_Attr = "request_receiver";
    public final static String BodyReceiver_Attr = "body_receiver";
    public final static String RequestPerformer_Attr = "request_performer";
    public final static String ReplySender_Attr = "reply_sender";
    public final static String BufferSender_Attr = "buffer_sender";
    public final static String ProxyConnector_Attr = "proxy_connector";
    public final static String AjpProxier_Attr = "ajp_proxier";
    public final static String HttpProxier_Attr = "http_proxier";
    public final static String Name_Attr = "name";
    public final static String Domain_Attr = "domain";
    public final static String Ip_Attr = "ip";
    public final static String Port_Attr = "port";
    public final static String Default_Host = "default_host";
    public final static String Default_Charset_Attr = "default_charset";
    public final static String Default_Allowed_Methods = "default_allowed_methods";
    public final static String EXT_Attr = "ext";
    public final static String Type_Attr = "type";
    public final static String Key_Alias_Attr = "key_alias";
    public final static String Certificate_Verification_Attr = "certificate_verification";
    public final static String Provider_Attr = "provider";
    public final static String Balance_Attr = "balance";
    public final static String Protocol_Attr = "protocol";
    public final static String IP_Or_Domain_Attr = "ip_or_domain";
    public final static String Keep_Alive_Timeout_Attr = "keep_alive_timeout";
    public final static String Idle_Timeout_Attr = "idle_timeout";
    public final static String URL_Attr = "url";
    public final static String Name_Pattern_Attr = "name_pattern";
    public final static String Inheritable_Attr = "inheritable";
    public final static String Realm_Description_Attr = "realm_description";
    public final static String Username_Attr = "username";
    public final static String Password_Attr = "password";
    public final static String Methods_Attr = "methods";
    public final static String Rest_Source_URL_Pattern_Attr = "rest_source_url_pattern";
    public final static String Reply_Code_Attr = "reply_code";
    public final static String Target_URL_Attr = "target_url";
    public final static String Sort_Attr = "sort";
    public final static String Value_Attr = "value";
    public final static String Source_Charset_Attr = "source_charset";
    public final static String Target_Charset_Attr = "target_charset";
    public final static String Coding_Attr = "coding";
    public final static String Header_Attr = "header";
    public final static String Compare_Op_Attr = "compare_op";
    public final static String Condition_Op_Attr = "condition_op";

    public final static String True_Value = "true";
    public final static String Round_Robin_Value = "round_robin";
    public final static String Least_Connection_Value = "least_connection";
    public final static String Least_Load_Value = "least_load";

    public final static String Comma = ",";

    public static String getCDATA(Element element) {
        Node child = element.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

}
