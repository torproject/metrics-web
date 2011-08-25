package org.torproject.ernie.web;

import java.util.*;

public class Countries {

  private static Countries instance = new Countries();

  public static Countries getInstance() {
    return Countries.instance;
  }

  /* List of arrays of length 2, containing country codes at [0] and
   * country names at [1], alphabetically ordered by country names. */
  private List<String[]> knownCountries;

  private Countries() {
    this.knownCountries = new ArrayList<String[]>();
    this.knownCountries.add("af;Afghanistan".split(";"));
    this.knownCountries.add("ax;Aland Islands".split(";"));
    this.knownCountries.add("al;Albania".split(";"));
    this.knownCountries.add("dz;Algeria".split(";"));
    this.knownCountries.add("as;American Samoa".split(";"));
    this.knownCountries.add("ad;Andorra".split(";"));
    this.knownCountries.add("ao;Angola".split(";"));
    this.knownCountries.add("ai;Anguilla".split(";"));
    this.knownCountries.add("aq;Antarctica".split(";"));
    this.knownCountries.add("ag;Antigua and Barbuda".split(";"));
    this.knownCountries.add("ar;Argentina".split(";"));
    this.knownCountries.add("am;Armenia".split(";"));
    this.knownCountries.add("aw;Aruba".split(";"));
    this.knownCountries.add("au;Australia".split(";"));
    this.knownCountries.add("at;Austria".split(";"));
    this.knownCountries.add("az;Azerbaijan".split(";"));
    this.knownCountries.add("bs;Bahamas".split(";"));
    this.knownCountries.add("bh;Bahrain".split(";"));
    this.knownCountries.add("bd;Bangladesh".split(";"));
    this.knownCountries.add("bb;Barbados".split(";"));
    this.knownCountries.add("by;Belarus".split(";"));
    this.knownCountries.add("be;Belgium".split(";"));
    this.knownCountries.add("bz;Belize".split(";"));
    this.knownCountries.add("bj;Benin".split(";"));
    this.knownCountries.add("bm;Bermuda".split(";"));
    this.knownCountries.add("bt;Bhutan".split(";"));
    this.knownCountries.add("bo;Bolivia".split(";"));
    this.knownCountries.add("ba;Bosnia and Herzegovina".split(";"));
    this.knownCountries.add("bw;Botswana".split(";"));
    this.knownCountries.add("bv;Bouvet Island".split(";"));
    this.knownCountries.add("br;Brazil".split(";"));
    this.knownCountries.add("io;British Indian Ocean Territory".
        split(";"));
    this.knownCountries.add("bn;Brunei Darussalam".split(";"));
    this.knownCountries.add("bg;Bulgaria".split(";"));
    this.knownCountries.add("bf;Burkina Faso".split(";"));
    this.knownCountries.add("mm;Burma".split(";"));
    this.knownCountries.add("bi;Burundi".split(";"));
    this.knownCountries.add("kh;Cambodia".split(";"));
    this.knownCountries.add("cm;Cameroon".split(";"));
    this.knownCountries.add("ca;Canada".split(";"));
    this.knownCountries.add("cv;Cape Verde".split(";"));
    this.knownCountries.add("ky;Cayman Islands".split(";"));
    this.knownCountries.add("cf;Central African Republic".split(";"));
    this.knownCountries.add("td;Chad".split(";"));
    this.knownCountries.add("cl;Chile".split(";"));
    this.knownCountries.add("cn;China".split(";"));
    this.knownCountries.add("cx;Christmas Island".split(";"));
    this.knownCountries.add("cc;Cocos (Keeling) Islands".split(";"));
    this.knownCountries.add("co;Colombia".split(";"));
    this.knownCountries.add("km;Comoros".split(";"));
    this.knownCountries.add("cd;Congo, The Democratic Republic of the".
        split(";"));
    this.knownCountries.add("cg;Congo".split(";"));
    this.knownCountries.add("ck;Cook Islands".split(";"));
    this.knownCountries.add("cr;Costa Rica".split(";"));
    this.knownCountries.add("ci;Cote d'Ivoire".split(";"));
    this.knownCountries.add("hr;Croatia".split(";"));
    this.knownCountries.add("cu;Cuba".split(";"));
    this.knownCountries.add("cy;Cyprus".split(";"));
    this.knownCountries.add("cz;Czech Republic".split(";"));
    this.knownCountries.add("dk;Denmark".split(";"));
    this.knownCountries.add("dj;Djibouti".split(";"));
    this.knownCountries.add("dm;Dominica".split(";"));
    this.knownCountries.add("do;Dominican Republic".split(";"));
    this.knownCountries.add("ec;Ecuador".split(";"));
    this.knownCountries.add("eg;Egypt".split(";"));
    this.knownCountries.add("sv;El Salvador".split(";"));
    this.knownCountries.add("gq;Equatorial Guinea".split(";"));
    this.knownCountries.add("er;Eritrea".split(";"));
    this.knownCountries.add("ee;Estonia".split(";"));
    this.knownCountries.add("et;Ethiopia".split(";"));
    this.knownCountries.add("fk;Falkland Islands (Malvinas)".split(";"));
    this.knownCountries.add("fo;Faroe Islands".split(";"));
    this.knownCountries.add("fj;Fiji".split(";"));
    this.knownCountries.add("fi;Finland".split(";"));
    this.knownCountries.add("fx;France, Metropolitan".split(";"));
    this.knownCountries.add("fr;France".split(";"));
    this.knownCountries.add("gf;French Guiana".split(";"));
    this.knownCountries.add("pf;French Polynesia".split(";"));
    this.knownCountries.add("tf;French Southern Territories".split(";"));
    this.knownCountries.add("ga;Gabon".split(";"));
    this.knownCountries.add("gm;Gambia".split(";"));
    this.knownCountries.add("ge;Georgia".split(";"));
    this.knownCountries.add("de;Germany".split(";"));
    this.knownCountries.add("gh;Ghana".split(";"));
    this.knownCountries.add("gi;Gibraltar".split(";"));
    this.knownCountries.add("gr;Greece".split(";"));
    this.knownCountries.add("gl;Greenland".split(";"));
    this.knownCountries.add("gd;Grenada".split(";"));
    this.knownCountries.add("gp;Guadeloupe".split(";"));
    this.knownCountries.add("gu;Guam".split(";"));
    this.knownCountries.add("gt;Guatemala".split(";"));
    this.knownCountries.add("gg;Guernsey".split(";"));
    this.knownCountries.add("gn;Guinea".split(";"));
    this.knownCountries.add("gw;Guinea-Bissau".split(";"));
    this.knownCountries.add("gy;Guyana".split(";"));
    this.knownCountries.add("ht;Haiti".split(";"));
    this.knownCountries.add("hm;Heard Island and McDonald Islands".
        split(";"));
    this.knownCountries.add("va;Holy See (Vatican City State)".
        split(";"));
    this.knownCountries.add("hn;Honduras".split(";"));
    this.knownCountries.add("hk;Hong Kong".split(";"));
    this.knownCountries.add("hu;Hungary".split(";"));
    this.knownCountries.add("is;Iceland".split(";"));
    this.knownCountries.add("in;India".split(";"));
    this.knownCountries.add("id;Indonesia".split(";"));
    this.knownCountries.add("ir;Iran, Islamic Republic of".split(";"));
    this.knownCountries.add("iq;Iraq".split(";"));
    this.knownCountries.add("ie;Ireland".split(";"));
    this.knownCountries.add("im;Isle of Man".split(";"));
    this.knownCountries.add("il;Israel".split(";"));
    this.knownCountries.add("it;Italy".split(";"));
    this.knownCountries.add("jm;Jamaica".split(";"));
    this.knownCountries.add("jp;Japan".split(";"));
    this.knownCountries.add("je;Jersey".split(";"));
    this.knownCountries.add("jo;Jordan".split(";"));
    this.knownCountries.add("kz;Kazakhstan".split(";"));
    this.knownCountries.add("ke;Kenya".split(";"));
    this.knownCountries.add("ki;Kiribati".split(";"));
    this.knownCountries.add("kp;Korea, Democratic People's Republic of".
        split(";"));
    this.knownCountries.add("kr;Korea, Republic of".split(";"));
    this.knownCountries.add("kw;Kuwait".split(";"));
    this.knownCountries.add("kg;Kyrgyzstan".split(";"));
    this.knownCountries.add("la;Lao People's Democratic Republic".
        split(";"));
    this.knownCountries.add("lv;Latvia".split(";"));
    this.knownCountries.add("lb;Lebanon".split(";"));
    this.knownCountries.add("ls;Lesotho".split(";"));
    this.knownCountries.add("lr;Liberia".split(";"));
    this.knownCountries.add("ly;Libya".split(";"));
    this.knownCountries.add("li;Liechtenstein".split(";"));
    this.knownCountries.add("lt;Lithuania".split(";"));
    this.knownCountries.add("lu;Luxembourg".split(";"));
    this.knownCountries.add("mo;Macao".split(";"));
    this.knownCountries.add("mk;Macedonia".split(";"));
    this.knownCountries.add("mg;Madagascar".split(";"));
    this.knownCountries.add("mw;Malawi".split(";"));
    this.knownCountries.add("my;Malaysia".split(";"));
    this.knownCountries.add("mv;Maldives".split(";"));
    this.knownCountries.add("ml;Mali".split(";"));
    this.knownCountries.add("mt;Malta".split(";"));
    this.knownCountries.add("mh;Marshall Islands".split(";"));
    this.knownCountries.add("mq;Martinique".split(";"));
    this.knownCountries.add("mr;Mauritania".split(";"));
    this.knownCountries.add("mu;Mauritius".split(";"));
    this.knownCountries.add("yt;Mayotte".split(";"));
    this.knownCountries.add("mx;Mexico".split(";"));
    this.knownCountries.add("fm;Micronesia, Federated States of".
        split(";"));
    this.knownCountries.add("md;Moldova, Republic of".split(";"));
    this.knownCountries.add("mc;Monaco".split(";"));
    this.knownCountries.add("mn;Mongolia".split(";"));
    this.knownCountries.add("me;Montenegro".split(";"));
    this.knownCountries.add("ms;Montserrat".split(";"));
    this.knownCountries.add("ma;Morocco".split(";"));
    this.knownCountries.add("mz;Mozambique".split(";"));
    this.knownCountries.add("mm;Myanmar".split(";"));
    this.knownCountries.add("na;Namibia".split(";"));
    this.knownCountries.add("nr;Nauru".split(";"));
    this.knownCountries.add("np;Nepal".split(";"));
    this.knownCountries.add("an;Netherlands Antilles".split(";"));
    this.knownCountries.add("nl;Netherlands".split(";"));
    this.knownCountries.add("nc;New Caledonia".split(";"));
    this.knownCountries.add("nz;New Zealand".split(";"));
    this.knownCountries.add("ni;Nicaragua".split(";"));
    this.knownCountries.add("ne;Niger".split(";"));
    this.knownCountries.add("ng;Nigeria".split(";"));
    this.knownCountries.add("nu;Niue".split(";"));
    this.knownCountries.add("nf;Norfolk Island".split(";"));
    this.knownCountries.add("mp;Northern Mariana Islands".split(";"));
    this.knownCountries.add("no;Norway".split(";"));
    this.knownCountries.add("om;Oman".split(";"));
    this.knownCountries.add("pk;Pakistan".split(";"));
    this.knownCountries.add("pw;Palau".split(";"));
    this.knownCountries.add("ps;Palestinian Territory".split(";"));
    this.knownCountries.add("pa;Panama".split(";"));
    this.knownCountries.add("pg;Papua New Guinea".split(";"));
    this.knownCountries.add("py;Paraguay".split(";"));
    this.knownCountries.add("pe;Peru".split(";"));
    this.knownCountries.add("ph;Philippines".split(";"));
    this.knownCountries.add("pn;Pitcairn".split(";"));
    this.knownCountries.add("pl;Poland".split(";"));
    this.knownCountries.add("pt;Portugal".split(";"));
    this.knownCountries.add("pr;Puerto Rico".split(";"));
    this.knownCountries.add("qa;Qatar".split(";"));
    this.knownCountries.add("re;Reunion".split(";"));
    this.knownCountries.add("ro;Romania".split(";"));
    this.knownCountries.add("ru;Russian Federation".split(";"));
    this.knownCountries.add("rw;Rwanda".split(";"));
    this.knownCountries.add("bl;Saint Bartelemey".split(";"));
    this.knownCountries.add("sh;Saint Helena".split(";"));
    this.knownCountries.add("kn;Saint Kitts and Nevis".split(";"));
    this.knownCountries.add("lc;Saint Lucia".split(";"));
    this.knownCountries.add("mf;Saint Martin".split(";"));
    this.knownCountries.add("pm;Saint Pierre and Miquelon".split(";"));
    this.knownCountries.add("vc;Saint Vincent and the Grenadines".
        split(";"));
    this.knownCountries.add("ws;Samoa".split(";"));
    this.knownCountries.add("sm;San Marino".split(";"));
    this.knownCountries.add("st;Sao Tome and Principe".split(";"));
    this.knownCountries.add("sa;Saudi Arabia".split(";"));
    this.knownCountries.add("sn;Senegal".split(";"));
    this.knownCountries.add("rs;Serbia".split(";"));
    this.knownCountries.add("sc;Seychelles".split(";"));
    this.knownCountries.add("sl;Sierra Leone".split(";"));
    this.knownCountries.add("sg;Singapore".split(";"));
    this.knownCountries.add("sk;Slovakia".split(";"));
    this.knownCountries.add("si;Slovenia".split(";"));
    this.knownCountries.add("sb;Solomon Islands".split(";"));
    this.knownCountries.add("so;Somalia".split(";"));
    this.knownCountries.add("za;South Africa".split(";"));
    this.knownCountries.add(("gs;South Georgia and the South Sandwich "
        + "Islands").split(";"));
    this.knownCountries.add("es;Spain".split(";"));
    this.knownCountries.add("lk;Sri Lanka".split(";"));
    this.knownCountries.add("sd;Sudan".split(";"));
    this.knownCountries.add("sr;Suriname".split(";"));
    this.knownCountries.add("sj;Svalbard and Jan Mayen".split(";"));
    this.knownCountries.add("sz;Swaziland".split(";"));
    this.knownCountries.add("se;Sweden".split(";"));
    this.knownCountries.add("ch;Switzerland".split(";"));
    this.knownCountries.add("sy;Syrian Arab Republic".split(";"));
    this.knownCountries.add("tw;Taiwan".split(";"));
    this.knownCountries.add("tj;Tajikistan".split(";"));
    this.knownCountries.add("tz;Tanzania, United Republic of".split(";"));
    this.knownCountries.add("th;Thailand".split(";"));
    this.knownCountries.add("tl;Timor-Leste".split(";"));
    this.knownCountries.add("tg;Togo".split(";"));
    this.knownCountries.add("tk;Tokelau".split(";"));
    this.knownCountries.add("to;Tonga".split(";"));
    this.knownCountries.add("tt;Trinidad and Tobago".split(";"));
    this.knownCountries.add("tn;Tunisia".split(";"));
    this.knownCountries.add("tr;Turkey".split(";"));
    this.knownCountries.add("tm;Turkmenistan".split(";"));
    this.knownCountries.add("tc;Turks and Caicos Islands".split(";"));
    this.knownCountries.add("tv;Tuvalu".split(";"));
    this.knownCountries.add("ug;Uganda".split(";"));
    this.knownCountries.add("ua;Ukraine".split(";"));
    this.knownCountries.add("ae;United Arab Emirates".split(";"));
    this.knownCountries.add("gb;United Kingdom".split(";"));
    this.knownCountries.add("um;United States Minor Outlying Islands".
        split(";"));
    this.knownCountries.add("us;United States".split(";"));
    this.knownCountries.add("uy;Uruguay".split(";"));
    this.knownCountries.add("uz;Uzbekistan".split(";"));
    this.knownCountries.add("vu;Vanuatu".split(";"));
    this.knownCountries.add("ve;Venezuela".split(";"));
    this.knownCountries.add("vn;Vietnam".split(";"));
    this.knownCountries.add("vg;Virgin Islands, British".split(";"));
    this.knownCountries.add("vi;Virgin Islands, U.S.".split(";"));
    this.knownCountries.add("wf;Wallis and Futuna".split(";"));
    this.knownCountries.add("eh;Western Sahara".split(";"));
    this.knownCountries.add("ye;Yemen".split(";"));
    this.knownCountries.add("zm;Zambia".split(";"));
    this.knownCountries.add("zw;Zimbabwe".split(";"));
  }

  public List<String[]> getCountryList() {
    return this.knownCountries;
  }
}

