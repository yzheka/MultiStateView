# [![](https://jitpack.io/v/yzheka/MultiStateView.svg)](https://jitpack.io/#yzheka/MultiStateView) MultiStateView

see app module for sample

View supports following attributes:

|Attribute             |Description                                      |
|----------------------|-------------------------------------------------|
|inAnimation           |sets the animation to use when view is shown     |
|outAnimation          |sets the animation to use when view is hidden    |
|animationsEnabled     |enables or disables animations                   |
|inAnimationDuration   |overrides in animation duration                  |
|outAnimationDuration  |overrides out animation duration                 |
|initialState          |sets the state that will be used at the beginning|

Each child of this view supports fillowing attributes:

|Attribute             |Description                                       |
|----------------------|--------------------------------------------------|
|layout_state          |The state at which to show this child             |
|layout_hideStrategy   |The way how to hide this child (gone or invisible)|

Demo:

![](device-2019-11-06-150532.gif)
