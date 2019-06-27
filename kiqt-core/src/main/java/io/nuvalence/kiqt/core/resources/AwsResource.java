package io.nuvalence.kiqt.core.resources;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses resource and service information for an Amazon Resource Name.
 *
 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">AWS ARNs and namespaces documentation</a>
 */
public class AwsResource {
    private static final Pattern pattern = Pattern.compile(
        "^arn:aws:(?<service>\\w+):(?<region>[\\w-]+)?:(?<accountId>\\d{12})?:"
            + "((?<resourceType>\\w+)[/:])?(?<resource>[\\w_-]+)([/:](?<qualifier>.*))?$"
    );
    private String arn;
    private String resource;
    private String service;
    private String region;

    /**
     * Creates a well defined resource by parsing the arn.
     *
     * @param arn amazon resource name
     */
    public AwsResource(String arn) {
        if (arn == null || arn.isEmpty()) {
            throw new IllegalArgumentException("arn must not be null or empty");
        }
        this.arn = arn;
        Matcher m = pattern.matcher(arn);
        if (!m.matches()) {
            throw new IllegalArgumentException("illegal arn: " + arn);
        }
        resource = m.group("resource");
        service = m.group("service");
        region = m.group("region");
    }

    /**
     * Gets full AWS resource name.
     *
     * @return arn
     */
    public String getArn() {
        return arn;
    }

    /**
     * Gets the associated AWS resource (eg: name of the stream, cluster, s3 bucket, etc).
     *
     * @return resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Gets the associated AWS service (eg: kinesis, firehose, s3, ...).
     *
     * @return service
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the resource's region.
     *
     * @return region id
     */
    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AwsResource)) {
            return false;
        }

        AwsResource that = (AwsResource) o;

        return Objects.equals(this.arn, that.arn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arn);
    }
}
