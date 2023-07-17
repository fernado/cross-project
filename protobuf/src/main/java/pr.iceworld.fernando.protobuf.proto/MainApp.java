package pr.iceworld.fernando.protobuf.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.Arrays;

public class MainApp {

    public static void main(String[] args) {
        // builder for customer
        CustomerProto.Customer.Builder customerBuilder = CustomerProto.Customer.newBuilder();
        customerBuilder.setId(1);
        customerBuilder.setName("fernando");
        customerBuilder.setGender(CustomerProto.Gender.MALE);

        // builder for address
        CustomerProto.Customer.Address.Builder addressBuilder1 =
                CustomerProto.Customer.Address.newBuilder();
        addressBuilder1.setId(1);
        addressBuilder1.setDefault(true);
        addressBuilder1.setValue("XXXX AAAA BBBB");
        customerBuilder.addAddresses(addressBuilder1);
        // builder for address
        CustomerProto.Customer.Address.Builder addressBuilder2 =
                CustomerProto.Customer.Address.newBuilder();
        addressBuilder2.setId(2);
        addressBuilder2.setDefault(false);
        addressBuilder2.setValue("YYYY CCCCC DDDD");
        customerBuilder.addAddresses(addressBuilder2);

        // builder for phone
        PhoneProto.Phone.Builder phoneBuilder = PhoneProto.Phone.newBuilder();
        phoneBuilder.setBrand(PhoneProto.Brand.APPLE);
        phoneBuilder.setId(1);
        phoneBuilder.setValue("8610000");
        customerBuilder.setPhone(phoneBuilder);

        // attrs Map<String, String>
        customerBuilder.putAttrs("Sports", "Running");
        customerBuilder.putAttrs("Food", "Chicken");

        CustomerProto.Customer customer = customerBuilder.build();
        try {
            String toJsonOri = JsonFormat.printer().print(customer);
            System.out.println(String.format("json result=%s", toJsonOri));
            System.out.println(String.format("json size=%s", toJsonOri.getBytes().length));
            // serializable
            byte[] bytes4Customer = customer.toByteArray();
            System.out.println(String.format("bytes4Customer=%s", Arrays.toString(bytes4Customer)));
            System.out.println(String.format("bytes4Customer's length=%s", bytes4Customer.length));
            // deserializable
            CustomerProto.Customer deCustomer = CustomerProto.Customer.parseFrom(bytes4Customer);
            // can change to json format
            String toJsonDe = JsonFormat.printer().print(deCustomer);
            System.out.println(String.format("json result=%s", toJsonDe));
            System.out.println(String.format("json size=%s", toJsonDe.getBytes().length));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
