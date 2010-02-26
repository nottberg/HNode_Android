// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

import android.net.Uri;
import android.provider.BaseColumns;

public final class HNode {

    public static final String EPAUTHORITY = "org.hnode.endpointprovider";
    public static final String SWAUTHORITY = "org.hnode.switchprovider";

    // This class cannot be instantiated
    private HNode() {}
    
    /**
     * Notes table
     */
    public static final class Nodes implements BaseColumns {
        // This class cannot be instantiated
        private Nodes() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + EPAUTHORITY + "/nodes");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of nodes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hnode.node";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single node.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.hnode.node";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "updated DESC";

        /**
         * The type string for an endpoint.
         * <P>Type: TEXT</P>
         */
        public static final String ENDPOINT = "endpoint";

        /**
         * The revision information for the endpoint.
         * <P>Type: TEXT</P>
         */
        public static final String REVISION = "revision";

        /**
         * The hnode uid as a hex string
         * <P>Type: TEXT</P>
         */
        public static final String UID = "uid";

        /**
         * A nickname for the node. (More user friendly identification.)
         * <P>Type: TEXT</P>
         */
        public static final String NICKNAME = "nickname";

        /**
         * The ssid for the wifi network the data was collected on.
         * <P>Type: TEXT</P>
         */
        public static final String SSID = "ssid";

        /**
         * The URI for the hnode
         * <P>Type: TEXT</P>
         */
        public static final String NODEURI = "hnode_uri";

        /**
         * The End Point Index
         * <P>Type: INTEGER</P>
         */
        public static final String EPINDEX = "epindex";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String UPDATED_DATE = "updated";
    }
    
    public static final class Switch implements BaseColumns {
        // This class cannot be instantiated
        private Switch() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + SWAUTHORITY + "/switches");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of nodes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hnode.switch";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single node.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.hnode.switch";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "updated DESC";

        /**
         * The type integer for an index on the remote side.
         * <P>Type: INTEGER</P>
         */
        public static final String REMOTE_ID = "remoteid";

        /**
         * The id string for the switch.
         * <P>Type: TEXT</P>
         */
        public static final String IDSTR = "idstr";

        /**
         * The name of the switch
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * The hnode uid as a hex string
         * <P>Type: TEXT</P>
         */
        public static final String UID = "uid";

        /**
         * A comma seperated capabilities string. (ie. dimmable, etc.)
         * <P>Type: TEXT</P>
         */
        public static final String CAPABILITIES = "capabilities";

        /**
         * The hnode uri that is used to control this switch
         * <P>Type: TEXT</P>
         */
        public static final String NODEURI = "hnode_uri";

        /**
         * The ssid for the wifi network the data was collected on.
         * <P>Type: TEXT</P>
         */
        public static final String SSID = "ssid";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String UPDATED_DATE = "updated";
    }
}
