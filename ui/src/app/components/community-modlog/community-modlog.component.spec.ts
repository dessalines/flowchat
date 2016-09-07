/* tslint:disable:no-unused-variable */

import { By }           from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import {
  beforeEach, beforeEachProviders,
  describe, xdescribe,
  expect, it, xit,
  async, inject
} from '@angular/core/testing';

import { CommunityModlogComponent } from './community-modlog.component';

describe('Component: CommunityModlog', () => {
  it('should create an instance', () => {
    let component = new CommunityModlogComponent(null,null,null,null);
    expect(component).toBeTruthy();
  });
});
