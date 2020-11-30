package gov.nist.javax.sip.address;

import gov.nist.core.Separators;
import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.parser.URLParser;
import java.text.ParseException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import javax.sip.address.URI;

public class AddressFactoryImpl implements AddressFactory {
    @Override // javax.sip.address.AddressFactory
    public Address createAddress() {
        return new AddressImpl();
    }

    @Override // javax.sip.address.AddressFactory
    public Address createAddress(String displayName, URI uri) {
        if (uri != null) {
            AddressImpl addressImpl = new AddressImpl();
            if (displayName != null) {
                addressImpl.setDisplayName(displayName);
            }
            addressImpl.setURI(uri);
            return addressImpl;
        }
        throw new NullPointerException("null  URI");
    }

    @Override // javax.sip.address.AddressFactory
    public SipURI createSipURI(String uri) throws ParseException {
        if (uri != null) {
            try {
                return new StringMsgParser().parseSIPUrl(uri);
            } catch (ParseException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }
        } else {
            throw new NullPointerException("null URI");
        }
    }

    @Override // javax.sip.address.AddressFactory
    public SipURI createSipURI(String user, String host) throws ParseException {
        if (host != null) {
            StringBuffer uriString = new StringBuffer("sip:");
            if (user != null) {
                uriString.append(user);
                uriString.append(Separators.AT);
            }
            if (!(host.indexOf(58) == host.lastIndexOf(58) || host.trim().charAt(0) == '[')) {
                host = '[' + host + ']';
            }
            uriString.append(host);
            try {
                return new StringMsgParser().parseSIPUrl(uriString.toString());
            } catch (ParseException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }
        } else {
            throw new NullPointerException("null host");
        }
    }

    @Override // javax.sip.address.AddressFactory
    public TelURL createTelURL(String uri) throws ParseException {
        if (uri != null) {
            try {
                return (TelURLImpl) new StringMsgParser().parseUrl("tel:" + uri);
            } catch (ParseException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }
        } else {
            throw new NullPointerException("null url");
        }
    }

    @Override // javax.sip.address.AddressFactory
    public Address createAddress(URI uri) {
        if (uri != null) {
            AddressImpl addressImpl = new AddressImpl();
            addressImpl.setURI(uri);
            return addressImpl;
        }
        throw new NullPointerException("null address");
    }

    @Override // javax.sip.address.AddressFactory
    public Address createAddress(String address) throws ParseException {
        if (address == null) {
            throw new NullPointerException("null address");
        } else if (!address.equals(Separators.STAR)) {
            return new StringMsgParser().parseAddress(address);
        } else {
            AddressImpl addressImpl = new AddressImpl();
            addressImpl.setAddressType(3);
            SipURI uri = new SipUri();
            uri.setUser(Separators.STAR);
            addressImpl.setURI(uri);
            return addressImpl;
        }
    }

    @Override // javax.sip.address.AddressFactory
    public URI createURI(String uri) throws ParseException {
        if (uri != null) {
            try {
                URLParser urlParser = new URLParser(uri);
                String scheme = urlParser.peekScheme();
                if (scheme == null) {
                    throw new ParseException("bad scheme", 0);
                } else if (scheme.equalsIgnoreCase("sip")) {
                    return urlParser.sipURL(true);
                } else {
                    if (scheme.equalsIgnoreCase("sips")) {
                        return urlParser.sipURL(true);
                    }
                    if (scheme.equalsIgnoreCase("tel")) {
                        return urlParser.telURL(true);
                    }
                    return new GenericURI(uri);
                }
            } catch (ParseException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }
        } else {
            throw new NullPointerException("null arg");
        }
    }
}
