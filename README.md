# mCerebrum-DataKit

mCerebrum is a configurable smartphone software platform for mobile and wearable sensors. It provides support for reliable data collection from mobile and wearable sensors, and offers real-time processing of these data.

This android service is the core data layer of the mCerebrum platform.

You can find more information about MD2K software on our [software website](https://md2k.org/software) or the MD2K organization on our [MD2K website](https://md2k.org/).

## Installation

Download and install the latest [DataKit](https://github.com/MD2Korg/mCerebrum-DataKit/releases/latest) application along with any of the other applications that produce data.

#### Enable unsigned applications
- Go to Settings.
- Select Security.
- On the list, Find Unknown Sources and CHECK the box.
- When done, go back to the folder.
- Tap on the apk file and select Open/Install. It should be able to install.

## References
- [UbiComp 2015](http://ubicomp.org/ubicomp2015/program/accepted-papers.html)
*cStress: Towards a Gold Standard for Continuous Stress Assessment in the Mobile Environment*
Karen Hovsepian, Mustafa al'absi, Emre Ertin, Thomas Kamarck, Motoshiro Nakajima, Santosh Kumar [pdf](http://dl.acm.org/citation.cfm?id=2807526)

## Contributing
Please read our [Contributing Guidelines](https://md2k.org/software/under-the-hood/contributing) for details on the process for submitting pull requests to us.

We use the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Our [Code of Conduct](https://md2k.org/software/CodeofConduct) is the [Contributor Covenant](https://www.contributor-covenant.org/).

Bug reports can be submitted through [JIRA](https://md2korg.atlassian.net/secure/Dashboard.jspa).

Our discussion forum can be found [here](https://discuss.md2k.org/).

### Compilation
```
git clone https://github.com/MD2Korg/mCerebrum-Utilities.git
git clone https://github.com/MD2Korg/mCerebrum-DataKitAPI.git
git clone https://github.com/MD2Korg/mCerebrum-DataKit.git
```

## Versioning

We use [Semantic Versioning](https://semver.org/) for versioning the software which is based on the following guidelines.

MAJOR.MINOR.PATCH (example: 3.0.12)

  1. MAJOR version when incompatible API changes are made,
  2. MINOR version when functionality is added in a backwards-compatible manner, and
  3. PATCH version when backwards-compatible bug fixes are introduced.

For the versions available, see [this repository's tags](https://github.com/MD2Korg/mCerebrum-DataKit/tags).

## Contributors

Link to the [list of contributors](https://github.com/MD2Korg/mCerebrum-DataKit/graphs/contributors) who participated in this project.

## License

This project is licensed under the BSD 2-Clause - see the [license](https://md2k.org/software-under-the-hood/software-uth-license) file for details.

## Acknowledgments

* [National Institutes of Health](https://www.nih.gov/) - [Big Data to Knowledge Initiative](https://datascience.nih.gov/bd2k)
  * Grants: R01MD010362, 1UG1DA04030901, 1U54EB020404, 1R01CA190329, 1R01DE02524, R00MD010468, 3UH2DA041713, 10555SC
* [National Science Foundation](https://www.nsf.gov/)
  * Grants: 1640813, 1722646
* [Intelligence Advanced Research Projects Activity](https://www.iarpa.gov/)
  * Contract: 2017-17042800006
